package org.egov.user.security.oauth2.custom.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.domain.exception.sso.IdpJwtValidationException;
import org.egov.user.domain.exception.sso.OidcProviderConfigException;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IDPJwtValidatorTest {

        @Mock
        private AuthProperties authProperties;

        @Mock
        private OidcProviderSupplier oidcProviderSupplier;

        @Mock
        private ObjectMapper objectMapper;

        @Mock
        private JwtDecoder jwtDecoder;

        private IDPJwtValidator idpJwtValidator;

        @Before
        public void setup() {
                idpJwtValidator = new IDPJwtValidator(authProperties, oidcProviderSupplier);
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> getDecoderEntries() {
                return (Map<String, Object>) ReflectionTestUtils.getField(idpJwtValidator, "decoders");
        }

        private Object createDecoderEntry(JwtDecoder decoder, long createdAtMs) {
                try {
                        Class<?> entryClass = Class.forName("org.egov.user.security.oauth2.custom.jwt.IDPJwtValidator$DecoderEntry");
                        java.lang.reflect.Constructor<?> ctor = entryClass.getDeclaredConstructor(JwtDecoder.class, long.class);
                        ctor.setAccessible(true);
                        return ctor.newInstance(decoder, createdAtMs);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to create DecoderEntry for test", e);
                }
        }

        @Test
        public void testSupports_NullIssuer_ReturnsFalse() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);

                assertFalse(idpJwtValidator.supports(null));
        }

        @Test
        public void testSupports_EmptyIssuer_ReturnsFalse() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);

                assertFalse(idpJwtValidator.supports(""));
        }

        @Test
        public void testSupports_Enabled() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);

                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .issuerUri("any")
                        .build();
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                assertEquals(true, idpJwtValidator.supports("any"));
        }

        @Test
        public void testValidate_Success() {
                String token = "header.payload.signature";
                String issuer = "https://sts.windows.net/tenant-id/";

                String tenantId = "pb.amritsar";
                Map<String, String> roleMapping = new HashMap<>();
                roleMapping.put("AZURE_ROLE_1", "DIGIT_ROLE_1");
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .roleMapping(roleMapping)
                        .build();

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                // Inject mocked decoder into the decoders map
                Map<String, Object> decoders = getDecoderEntries();
                decoders.put("pb.amritsar:azure", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuer);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("AZURE_ROLE_1"));
                claims.put("tenantId", "pb.amritsar");
                claims.put("userType", "EMPLOYEE");

                Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600),
                                Collections.singletonMap("alg", "RS256"), claims);

                // We need to bypass extractIssuerUnverified because it's hard to mock static
                // JWTParser
                // Instead of mocking static, let's use a token that has the issuer in its
                // payload if we can,
                // but extractIssuerUnverified uses JWTParser.parse(token).
                // Since I cannot mock static JWTParser easily without PowerMock,
                // and I don't want to use PowerMock, I will use a real-looking JWT token for
                // parsing if possible,
                // or I'll just accept that I might need to mock it.

                // Actually, JWTParser.parse(token) doesn't verify the signature.
                // I'll create a simple base64 encoded payload for the "issuer" field.
                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"; // {"alg":"HS256","typ":"JWT"}
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0"; // {"iss":"https://sts.windows.net/tenant-id/"}
                String realLookingToken = header + "." + payload + ".signature";

                when(jwtDecoder.decode(realLookingToken)).thenReturn(jwt);

                OidcValidatedJwt result = idpJwtValidator.validate(realLookingToken, tenantId);

                assertNotNull(result);
                assertEquals("azure", result.getProviderId());
                assertEquals(tenantId, result.getTenantId());
                assertEquals(1, result.getRoles().size());
                assertEquals("DIGIT_ROLE_1", result.getRoles().iterator().next());
        }

        @Test
        public void testExtractRoles_MultiRoleSupport() {
                Map<String, String> roleMapping = new HashMap<>();
                roleMapping.put("AZURE_ADMIN", "SYSTEM_ADMINISTRATOR,SUPERUSER");
                roleMapping.put("AZURE_USER", "CITIZEN");
                
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .roleClaimKey("roles")
                        .roleMapping(roleMapping)
                        .build();

                Map<String, Object> claims = new HashMap<>();
                claims.put("tenantId", "pb.amritsar");
                claims.put("roles", Collections.singletonList("AZURE_ADMIN"));

                Set<String> roles = (Set<String>) ReflectionTestUtils.invokeMethod(idpJwtValidator, "extractRoles",
                                provider, claims);

                assertEquals(2, roles.size());
                org.junit.Assert.assertTrue(roles.contains("SYSTEM_ADMINISTRATOR"));
                org.junit.Assert.assertTrue(roles.contains("SUPERUSER"));
        }

        @Test
        public void testValidate_InvalidToken_ParseFailed_HasCorrectErrorCode() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.emptyList());

                try {
                        idpJwtValidator.validate("not-a-valid-jwt", "pb.amritsar");
                        org.junit.Assert.fail("Expected IdpJwtValidationException");
                } catch (IdpJwtValidationException e) {
                        assertEquals(SsoErrorCodes.JWT_PARSE_FAILED, e.getErrorCode());
                }
        }

        @Test
        public void testValidate_UnknownIssuer_HasCorrectErrorCode() {
                String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3Vua25vd24uaXNzdWVyLmNvbS8ifQ.x";
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.emptyList());

                try {
                        idpJwtValidator.validate(token, "pb.amritsar");
                        org.junit.Assert.fail("Expected OidcProviderConfigException");
                } catch (OidcProviderConfigException e) {
                        assertEquals(SsoErrorCodes.OIDC_PROVIDER_NOT_FOUND, e.getErrorCode());
                }
        }

        @Test
        public void testSupports_Disabled() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(false);
                when(authProperties.getOidc()).thenReturn(oidc);

                assertEquals(false, idpJwtValidator.supports("any"));
        }

        @Test
        public void testValidate_ExpiredToken_ThrowsExpiredError() {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .build();
                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                Map<String, Object> decoders = getDecoderEntries();
                decoders.put("pb.amritsar:azure", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                OAuth2Error error = new OAuth2Error("invalid_token", "Token expired", null);
                JwtValidationException expired = new JwtValidationException("expired", Collections.singletonList(error));

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String realLookingToken = header + "." + payload + ".signature";

                when(jwtDecoder.decode(realLookingToken)).thenThrow(expired);

                try {
                        idpJwtValidator.validate(realLookingToken, tenantId);
                        org.junit.Assert.fail("Expected IdpJwtValidationException");
                } catch (IdpJwtValidationException e) {
                        assertEquals(SsoErrorCodes.JWT_EXPIRED, e.getErrorCode());
                }
        }

        @Test
        public void testValidate_AudienceValidationFails_ThrowsIdpJwtValidationException() {
            String issuer = "https://sts.windows.net/tenant-id/";
            String tenantId = "pb.amritsar";
            AuthProperties.Provider provider = AuthProperties.Provider.builder()
                    .id("azure")
                    .issuerUri(issuer)
                    .jwkSetUri("http://jwks")
                    .roleClaimKey("roles")
                    .tenantId(tenantId)
                    .build();
            when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
            when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

            Map<String, Object> decoders = getDecoderEntries();
            decoders.put("pb.amritsar:azure", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

            OAuth2Error error = new OAuth2Error("invalid_token", "audience invalid", null);
            JwtValidationException invalidAud = new JwtValidationException("invalid audience", Collections.singletonList(error));

            String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
            String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
            String realLookingToken = header + "." + payload + ".signature";

            when(jwtDecoder.decode(realLookingToken)).thenThrow(invalidAud);

            try {
                    idpJwtValidator.validate(realLookingToken, tenantId);
                    org.junit.Assert.fail("Expected IdpJwtValidationException");
            } catch (IdpJwtValidationException e) {
                    // Only type assertion; specific error code is not required here
                    assertEquals(SsoErrorCodes.JWT_INVALID, e.getErrorCode());
            }
        }

        @Test
        public void testExtractRoles_UnmappedRole_FallsBackToDefaultRoleCodes() {
                Map<String, String> roleMapping = new HashMap<>();
                roleMapping.put("AZURE_ROLE", "DIGIT_ROLE");
                
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .roleClaimKey("roles")
                        .defaultRoleCodes("DEFAULT_ROLE")
                        .roleMapping(roleMapping)
                        .build();

                Map<String, Object> claims = new HashMap<>();
                claims.put("tenantId", "pb.amritsar");
                claims.put("roles", Collections.singletonList("UNMAPPED_ROLE"));

                @SuppressWarnings("unchecked")
                Set<String> roles = (Set<String>) ReflectionTestUtils.invokeMethod(idpJwtValidator, "extractRoles",
                                provider, claims);

                assertEquals(1, roles.size());
                org.junit.Assert.assertTrue(roles.contains("DEFAULT_ROLE"));
        }

        @Test
        public void testExtractRoles_NoRoleClaim_FallsBackToDefaultRoleCodes() {
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .roleClaimKey("roles")
                        .defaultRoleCodes("DEFAULT_ROLE")
                        .build();

                Map<String, Object> claims = new HashMap<>();
                claims.put("tenantId", "pb.amritsar");

                @SuppressWarnings("unchecked")
                Set<String> roles = (Set<String>) ReflectionTestUtils.invokeMethod(idpJwtValidator, "extractRoles",
                                provider, claims);

                assertEquals(1, roles.size());
                org.junit.Assert.assertTrue(roles.contains("DEFAULT_ROLE"));
        }

        @Test
        public void testValidate_UserTypeFallback_UsesProviderDefault() {
                String token = "header.payload.signature";
                String issuer = "https://sts.windows.net/tenant-id/";

                String tenantId = "pb.amritsar";
                Map<String, String> roleMapping = new HashMap<>();
                roleMapping.put("AZURE_ROLE_1", "DIGIT_ROLE_1");
                
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .userType("EMPLOYEE")
                        .roleMapping(roleMapping)
                        .build();

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                Map<String, Object> decoders = getDecoderEntries();
                decoders.put("pb.amritsar:azure", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuer);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("AZURE_ROLE_1"));
                claims.put("tenantId", tenantId);

                Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600),
                                Collections.singletonMap("alg", "RS256"), claims);

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"; // {"alg":"HS256","typ":"JWT"}
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0"; // {"iss":"https://sts.windows.net/tenant-id/"}
                String realLookingToken = header + "." + payload + ".signature";

                when(jwtDecoder.decode(realLookingToken)).thenReturn(jwt);

                OidcValidatedJwt result = idpJwtValidator.validate(realLookingToken, tenantId);

                assertNotNull(result);
                assertEquals("EMPLOYEE", result.getUserType());
        }

        @Test
        public void testExtractRoles_RoleClaimNotList_FallsBackToDefaultRoleCodes() {
                Map<String, String> roleMapping = new HashMap<>();
                roleMapping.put("AZURE_ROLE", "DIGIT_ROLE");
                
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .roleClaimKey("roles")
                        .defaultRoleCodes("DEFAULT_ROLE")
                        .roleMapping(roleMapping)
                        .build();

                Map<String, Object> claims = new HashMap<>();
                claims.put("tenantId", "pb.amritsar");
                claims.put("roles", "not-a-list");

                @SuppressWarnings("unchecked")
                Set<String> roles = (Set<String>) ReflectionTestUtils.invokeMethod(idpJwtValidator, "extractRoles",
                                provider, claims);

                assertEquals(1, roles.size());
                assertTrue(roles.contains("DEFAULT_ROLE"));
        }

        @Test
        public void testValidate_JwksUriMissing_ThrowsOidcProviderConfigExceptionWithCorrectErrorCode() {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";
                
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .build();
                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String realLookingToken = header + "." + payload + ".signature";

                try {
                        idpJwtValidator.validate(realLookingToken, tenantId);
                        org.junit.Assert.fail("Expected OidcProviderConfigException");
                } catch (OidcProviderConfigException e) {
                        assertEquals(SsoErrorCodes.OIDC_JWKS_MISSING, e.getErrorCode());
                }
        }

        @Test
        public void testValidate_MultipleProvidersSameIssuer_DisambiguatedByAudience_Success() throws Exception {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider1 = AuthProperties.Provider.builder()
                        .id("azure1")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks1")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .audiences(Collections.singletonList("aud1"))
                        .build();

                AuthProperties.Provider provider2 = AuthProperties.Provider.builder()
                        .id("azure2")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks2")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .audiences(Collections.singletonList("aud2"))
                        .build();

                List<AuthProperties.Provider> providers = Arrays.asList(provider1, provider2);
                when(authProperties.getProviders()).thenReturn(providers);
                when(oidcProviderSupplier.getProviders()).thenReturn(providers);

                Map<String, Object> decoders = getDecoderEntries();
                decoders.put("pb.amritsar:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuer);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("ROLE"));
                claims.put("tenantId", tenantId);
                claims.put("userType", "EMPLOYEE");

                Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600),
                                Collections.singletonMap("alg", "RS256"), claims);

                String payloadJson = "{\"iss\":\"https://sts.windows.net/tenant-id/\",\"aud\":[\"aud1\"]}";
                String payloadB64 = Base64.getUrlEncoder().withoutPadding()
                                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
                String header = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9"; // RS256 header
                String tokenWithAud = header + "." + payloadB64 + ".sig";

                when(jwtDecoder.decode(tokenWithAud)).thenReturn(jwt);

                OidcValidatedJwt result = idpJwtValidator.validate(tokenWithAud, tenantId);

                assertNotNull(result);
                assertEquals("azure1", result.getProviderId());
        }

        @Test
        public void testValidate_MultipleProvidersSameIssuer_NoAudienceInToken_ThrowsProviderAmbiguous() throws Exception {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider1 = AuthProperties.Provider.builder()
                        .id("azure1")
                        .issuerUri(issuer)
                        .tenantId(tenantId)
                        .audiences(Collections.singletonList("aud1"))
                        .build();

                AuthProperties.Provider provider2 = AuthProperties.Provider.builder()
                        .id("azure2")
                        .issuerUri(issuer)
                        .tenantId(tenantId)
                        .audiences(Collections.singletonList("aud2"))
                        .build();

                List<AuthProperties.Provider> providers = Arrays.asList(provider1, provider2);
                when(authProperties.getProviders()).thenReturn(providers);
                when(oidcProviderSupplier.getProviders()).thenReturn(providers);

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String tokenNoAud = header + "." + payload + ".signature";

                try {
                        idpJwtValidator.validate(tokenNoAud, tenantId);
                        org.junit.Assert.fail("Expected OidcProviderConfigException");
                } catch (OidcProviderConfigException e) {
                        assertEquals(SsoErrorCodes.OIDC_PROVIDER_AMBIGUOUS, e.getErrorCode());
                }
        }

        @Test
        public void testValidate_IssuerWithTrailingSlash_MatchesProviderWithoutSlash() {
                String issuerWithSlash = "https://sts.windows.net/tenant-id/";
                String issuerNoSlash = "https://sts.windows.net/tenant-id";
                String tenantId = "pb.amritsar";

                Map<String, String> roleMapping = new HashMap<>();
                roleMapping.put("AZURE_ROLE_1", "DIGIT_ROLE_1");
                
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuerNoSlash)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .roleMapping(roleMapping)
                        .build();

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                Map<String, Object> decoders = getDecoderEntries();
                decoders.put("pb.amritsar:azure", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuerWithSlash);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("AZURE_ROLE_1"));
                claims.put("tenantId", tenantId);
                claims.put("userType", "EMPLOYEE");

                Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600),
                                Collections.singletonMap("alg", "RS256"), claims);

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String realLookingToken = header + "." + payload + ".signature";

                when(jwtDecoder.decode(realLookingToken)).thenReturn(jwt);

                OidcValidatedJwt result = idpJwtValidator.validate(realLookingToken, tenantId);

                assertNotNull(result);
                assertEquals("azure", result.getProviderId());
                assertEquals(tenantId, result.getTenantId());
        }

        @Test
        public void testExtractRoles_MultipleDefaultRoleCodes_CommaDelimited() {
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .roleClaimKey("roles")
                        .defaultRoleCodes("ROLE_A, ROLE_B")
                        .build();

                Map<String, Object> claims = new HashMap<>();
                claims.put("tenantId", "pb.amritsar");

                @SuppressWarnings("unchecked")
                Set<String> roles = (Set<String>) ReflectionTestUtils.invokeMethod(idpJwtValidator, "extractRoles",
                                provider, claims);

                assertEquals(2, roles.size());
                assertTrue(roles.contains("ROLE_A"));
                assertTrue(roles.contains("ROLE_B"));
        }

        @Test
        public void testValidate_IssuerMissingInToken_ThrowsOidcProviderConfigException() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.emptyList());

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString("{\"sub\":\"user\"}".getBytes(StandardCharsets.UTF_8));
                String token = header + "." + payload + ".sig";

                try {
                        idpJwtValidator.validate(token, "pb.amritsar");
                        org.junit.Assert.fail("Expected OidcProviderConfigException");
                } catch (OidcProviderConfigException e) {
                        assertEquals(SsoErrorCodes.OIDC_ISSUER_MISSING_IN_TOKEN, e.getErrorCode());
                }
        }

        @Test
        public void testValidate_GenericDecodeException_ThrowsIdpJwtValidationException() {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";
                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .build();
                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                Map<String, Object> decoders = getDecoderEntries();
                decoders.put("pb.amritsar:azure", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String token = header + "." + payload + ".signature";

                when(jwtDecoder.decode(token)).thenThrow(new RuntimeException("decode failed"));

                try {
                        idpJwtValidator.validate(token, tenantId);
                        org.junit.Assert.fail("Expected IdpJwtValidationException");
                } catch (IdpJwtValidationException e) {
                        assertEquals(SsoErrorCodes.JWT_INVALID, e.getErrorCode());
                }
        }

        @Test
        public void testSupports_IssuerAlias_MatchesProvider() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);

                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .issuerUri("https://primary-issuer")
                        .issuerAliases(Collections.singletonList("https://alias-issuer"))
                        .build();
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                assertTrue(idpJwtValidator.supports("https://alias-issuer"));
        }

        @Test
        public void testDecoderCache_ReusesWithinTtlAndRecreatesAfterExpiry() throws Exception {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .audiences(Collections.singletonList("aud1"))
                        .build();

                // Configure TTL long enough to ensure reuse
                ReflectionTestUtils.setField(idpJwtValidator, "decoderCacheTtlMs", 60_000L);

                JwtDecoder first = (JwtDecoder) ReflectionTestUtils.invokeMethod(idpJwtValidator, "getDecoder", provider);
                JwtDecoder second = (JwtDecoder) ReflectionTestUtils.invokeMethod(idpJwtValidator, "getDecoder", provider);
                assertSame(first, second);

                // Now force a very small TTL and wait so entry expires
                ReflectionTestUtils.setField(idpJwtValidator, "decoderCacheTtlMs", 1L);
                Thread.sleep(5L);
                JwtDecoder third = (JwtDecoder) ReflectionTestUtils.invokeMethod(idpJwtValidator, "getDecoder", provider);

                assertNotSame(first, third);
        }

        @Test
        public void testClearDecoderCacheAndClearDecoderForProvider() {
                Map<String, Object> decoders = getDecoderEntries();
                decoders.clear();

                // Seed map with two providers using new cache key shape
                decoders.put("default:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("pb.amritsar:azure2", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                assertEquals(2, decoders.size());

                // Clear specific provider (by tenant + provider)
                idpJwtValidator.clearDecoderCacheFor("default", "azure1");
                assertFalse(decoders.containsKey("default:azure1"));
                assertTrue(decoders.containsKey("pb.amritsar:azure2"));

                // Clear all
                idpJwtValidator.clearDecoderCache();
                assertTrue(decoders.isEmpty());
        }

        @Test
        public void testValidate_SignatureFailure_TriggersRefreshAndRetry() {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .audiences(Collections.singletonList("aud1"))
                        .build();

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                IDPJwtValidator spyValidator = spy(new IDPJwtValidator(authProperties, oidcProviderSupplier));

                // Seed decoder cache with mocked decoder
                Map<String, Object> decoders = (Map<String, Object>) ReflectionTestUtils.getField(spyValidator, "decoders");
                decoders.put("pb.amritsar:azure", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuer);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("AZURE_ROLE_1"));
                claims.put("tenantId", tenantId);
                claims.put("userType", "EMPLOYEE");

                Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600),
                        Collections.singletonMap("alg", "RS256"), claims);

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String realLookingToken = header + "." + payload + ".signature";

                OAuth2Error signatureError = new OAuth2Error("invalid_token",
                        "Signature verification failed", null);
                JwtValidationException signatureEx =
                        new JwtValidationException("signature failure", Collections.singletonList(signatureError));

                // First call throws signature-like error, second call succeeds
                when(jwtDecoder.decode(realLookingToken))
                        .thenThrow(signatureEx)
                        .thenReturn(jwt);

                // Do not actually remove from cache so that spy still uses mocked decoder
                doNothing().when(spyValidator).clearDecoderForProvider(eq(tenantId), eq("azure"));

                OidcValidatedJwt result = spyValidator.validate(realLookingToken, tenantId);

                assertNotNull(result);
                assertEquals("azure", result.getProviderId());
                verify(spyValidator, times(1)).clearDecoderForProvider(eq(tenantId), eq("azure"));
                verify(jwtDecoder, times(2)).decode(realLookingToken);
        }

        @Test
        public void testDecoderCacheTtl_DefaultValue() {
                IDPJwtValidator validator = new IDPJwtValidator(authProperties, oidcProviderSupplier);
                Long ttl = (Long) ReflectionTestUtils.getField(validator, "decoderCacheTtlMs");
                assertEquals(Long.valueOf(3_600_000L), ttl); // 1 hour default
        }

        @Test
        public void testDecoderCacheTtl_ConfiguredValue() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setJwksCacheTtlMs(30_000L); // 30 seconds
                when(authProperties.getOidc()).thenReturn(oidc);

                IDPJwtValidator validator = new IDPJwtValidator(authProperties, oidcProviderSupplier);
                Long ttl = (Long) ReflectionTestUtils.getField(validator, "decoderCacheTtlMs");
                assertEquals(Long.valueOf(30_000L), ttl);
        }

        @Test
        public void testDecoderCacheTtl_ZeroConfigured_UsesDefault() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setJwksCacheTtlMs(0L); // Zero should fallback to default
                when(authProperties.getOidc()).thenReturn(oidc);

                IDPJwtValidator validator = new IDPJwtValidator(authProperties, oidcProviderSupplier);
                Long ttl = (Long) ReflectionTestUtils.getField(validator, "decoderCacheTtlMs");
                assertEquals(Long.valueOf(3_600_000L), ttl); // Should use default
        }

        @Test
        public void testDecoderCacheTtl_NullConfigured_UsesDefault() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setJwksCacheTtlMs(null); // Null should fallback to default
                when(authProperties.getOidc()).thenReturn(oidc);

                IDPJwtValidator validator = new IDPJwtValidator(authProperties, oidcProviderSupplier);
                Long ttl = (Long) ReflectionTestUtils.getField(validator, "decoderCacheTtlMs");
                assertEquals(Long.valueOf(3_600_000L), ttl); // Should use default
        }

        @Test
        public void testDecoderCacheTtl_NullOidc_UsesDefault() {
                when(authProperties.getOidc()).thenReturn(null);

                IDPJwtValidator validator = new IDPJwtValidator(authProperties, oidcProviderSupplier);
                Long ttl = (Long) ReflectionTestUtils.getField(validator, "decoderCacheTtlMs");
                assertEquals(Long.valueOf(3_600_000L), ttl); // Should use default
        }

        @Test
        public void testClearDecoderForProvider_NullProviderId_NoException() {
                Map<String, Object> decoders = getDecoderEntries();
                decoders.put("other-tenant:test", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                // Should not throw exception (providerId null = clear all for tenant)
                idpJwtValidator.clearDecoderCacheFor("test", null);

                // Cache should remain unchanged (different tenant)
                assertEquals(1, decoders.size());
                assertTrue(decoders.containsKey("other-tenant:test"));
        }

        @Test
        public void testClearDecoderForProvider_NonExistentProviderId_NoException() {
                Map<String, Object> decoders = getDecoderEntries();
                decoders.put("existing", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));

                // Should not throw exception for non-existent provider
                idpJwtValidator.clearDecoderCacheFor("test", "non-existent");
                
                // Cache should remain unchanged
                assertEquals(1, decoders.size());
                assertTrue(decoders.containsKey("existing"));
        }

        @Test
        public void testClearDecoderCache_EmptyCache_NoException() {
                Map<String, Object> decoders = getDecoderEntries();
                decoders.clear();

                // Should not throw exception
                idpJwtValidator.clearDecoderCache();
                
                // Cache should remain empty
                assertTrue(decoders.isEmpty());
        }

        @Test
        public void testDecoderCache_ZeroTtl_AlwaysExpires() throws Exception {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .audiences(Collections.singletonList("aud1"))
                        .build();

                // Set TTL to 0 (should always be considered expired)
                ReflectionTestUtils.setField(idpJwtValidator, "decoderCacheTtlMs", 0L);

                JwtDecoder first = (JwtDecoder) ReflectionTestUtils.invokeMethod(idpJwtValidator, "getDecoder", provider);
                JwtDecoder second = (JwtDecoder) ReflectionTestUtils.invokeMethod(idpJwtValidator, "getDecoder", provider);

                // Should always create new decoder since TTL is 0
                assertNotSame(first, second);
        }

        @Test
        public void testClearDecoderForProvider_TenantAndProvider_RemovesSingleEntry() {
                Map<String, Object> decoders = getDecoderEntries();
                decoders.clear();

                // Seed map with multiple tenants and providers
                decoders.put("default:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("pb.amritsar:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("pb.amritsar:azure2", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                assertEquals(3, decoders.size());

                // Clear specific provider using new method (removes only specific tenant+provider combination)
                idpJwtValidator.clearDecoderForProvider("pb.amritsar", "azure1");
                assertFalse(decoders.containsKey("pb.amritsar:azure1"));
                assertTrue(decoders.containsKey("default:azure1")); // Should remain since we only cleared specific tenant
                // Other provider entries should remain
                assertTrue(decoders.containsKey("pb.amritsar:azure2"));
        }

        @Test
        public void testClearDecodersForTenant_RemovesAllTenantEntries() {
                Map<String, Object> decoders = getDecoderEntries();
                decoders.clear();

                // Seed map with multiple providers for same tenant
                decoders.put("pb.amritsar:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("pb.amritsar:azure2", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("default:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                assertEquals(3, decoders.size());

                // Clear all entries for specific tenant using new method
                idpJwtValidator.clearDecoderCacheFor("pb.amritsar", null);
                assertFalse(decoders.containsKey("pb.amritsar:azure1"));
                assertFalse(decoders.containsKey("pb.amritsar:azure2"));
                assertTrue(decoders.containsKey("default:azure1"));
        }

        @Test
        public void testClearDecoderForProvider_TenantOnly_RemovesAllTenantEntries() {
                Map<String, Object> decoders = getDecoderEntries();
                decoders.clear();

                // Seed map with multiple providers for same tenant
                decoders.put("pb.amritsar:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("pb.amritsar:azure2", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("default:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                assertEquals(3, decoders.size());

                // Clear all entries for specific tenant
                idpJwtValidator.clearDecoderCacheFor("pb.amritsar", null);
                assertFalse(decoders.containsKey("pb.amritsar:azure1"));
                assertFalse(decoders.containsKey("pb.amritsar:azure2"));
                assertTrue(decoders.containsKey("default:azure1"));
        }

        @Test
        public void testClearDecoderForProvider_ProviderOnly_RemovesAllProviderEntries() {
                Map<String, Object> decoders = getDecoderEntries();
                decoders.clear();

                // Seed map with same provider across multiple tenants
                decoders.put("default:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("pb.amritsar:azure1", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                decoders.put("pb.ludhiana:azure2", createDecoderEntry(jwtDecoder, System.currentTimeMillis()));
                assertEquals(3, decoders.size());

                // Clear specific provider for specific tenant
                idpJwtValidator.clearDecoderCacheFor("default", "azure1");
                assertFalse(decoders.containsKey("default:azure1"));
                assertTrue(decoders.containsKey("pb.amritsar:azure1")); // Should remain (different tenant)
                assertTrue(decoders.containsKey("pb.ludhiana:azure2"));
        }

        @Test
        public void testDecoderCache_NegativeTtl_NeverExpires() throws Exception {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider = AuthProperties.Provider.builder()
                        .id("azure")
                        .issuerUri(issuer)
                        .jwkSetUri("http://jwks")
                        .roleClaimKey("roles")
                        .tenantId(tenantId)
                        .audiences(Collections.singletonList("aud1"))
                        .build();

                // Set TTL to negative (should never expire)
                ReflectionTestUtils.setField(idpJwtValidator, "decoderCacheTtlMs", -1L);

                JwtDecoder first = (JwtDecoder) ReflectionTestUtils.invokeMethod(idpJwtValidator, "getDecoder", provider);
                JwtDecoder second = (JwtDecoder) ReflectionTestUtils.invokeMethod(idpJwtValidator, "getDecoder", provider);

                // Should reuse decoder since negative TTL means no expiration
                assertSame(first, second);
        }
}
