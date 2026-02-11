package org.egov.user.security.oauth2.custom.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

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

        @Mock
        private RestTemplate restTemplate;

        private IDPJwtValidator idpJwtValidator;

        @Before
        public void setup() {
                idpJwtValidator = new IDPJwtValidator(authProperties, oidcProviderSupplier, objectMapper, restTemplate);
        }

        @Test
        public void testSupports_Enabled() {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);

                assertEquals(true, idpJwtValidator.supports("any"));
        }

        @Test
        public void testValidate_Success() {
                String token = "header.payload.signature";
                String issuer = "https://sts.windows.net/tenant-id/";

                AuthProperties.Provider provider = new AuthProperties.Provider();
                provider.setId("azure");
                provider.setIssuerUri(issuer);
                provider.setJwkSetUri("http://jwks");
                provider.setRoleClaimKey("roles");
                provider.setProjectName("NMCP");
                provider.setHierarchyType("REVENUE");
                Map<String, String> roleMapping = new HashMap<>();
                roleMapping.put("AZURE_ROLE_1", "DIGIT_ROLE_1");
                ReflectionTestUtils.setField(provider, "roleMapping", roleMapping);

                // Manually inject role mapping into the cache to bypass MDMS call in test
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> roleMappingCache = (Map<String, Map<String, String>>) ReflectionTestUtils
                                .getField(idpJwtValidator, "roleMappingCache");
                roleMappingCache.put("azure:pb.amritsar", roleMapping);

                Map<String, String> boundaryMapping = new HashMap<>();
                boundaryMapping.put("AZURE_ROLE_1", "COUNTRY_CODE_TOGO");
                ReflectionTestUtils.setField(provider, "roleBoundaryMapping", boundaryMapping);

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                // Inject mocked decoder into the decoders map
                @SuppressWarnings("unchecked")
                Map<String, JwtDecoder> decoders = (Map<String, JwtDecoder>) ReflectionTestUtils.getField(
                                idpJwtValidator,
                                "decoders");
                decoders.put("azure", jwtDecoder);

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

                OidcValidatedJwt result = idpJwtValidator.validate(realLookingToken);

                assertNotNull(result);
                assertEquals("NMCP", result.getProjectName());
                assertEquals("COUNTRY_CODE_TOGO", result.getBoundary());
                assertEquals(1, result.getRoles().size());
                assertEquals("DIGIT_ROLE_1", result.getRoles().iterator().next());
        }

        @Test
        public void testValidate_DefaultBoundary() {
                String token = "header.payload.signature";
                String issuer = "https://sts.windows.net/tenant-id/";

                AuthProperties.Provider provider = new AuthProperties.Provider();
                provider.setId("azure");
                provider.setIssuerUri(issuer);
                provider.setJwkSetUri("http://jwks");
                provider.setRoleClaimKey("roles");
                provider.setDefaultBoundaryCode("DEFAULT_BOUNDARY");
                // No roleBoundaryMapping provided
                ReflectionTestUtils.setField(provider, "roleMapping",
                                Collections.singletonMap("AZURE_ROLE_1", "DIGIT_ROLE_1"));

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                @SuppressWarnings("unchecked")
                Map<String, JwtDecoder> decoders = (Map<String, JwtDecoder>) ReflectionTestUtils.getField(
                                idpJwtValidator,
                                "decoders");
                decoders.put("azure", jwtDecoder);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuer);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("AZURE_ROLE_1"));
                claims.put("tenantId", "pb.amritsar");
                claims.put("userType", "EMPLOYEE");

                Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600),
                                Collections.singletonMap("alg", "RS256"), claims);

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String realLookingToken = header + "." + payload + ".signature";

                when(jwtDecoder.decode(realLookingToken)).thenReturn(jwt);

                OidcValidatedJwt result = idpJwtValidator.validate(realLookingToken);

                assertNotNull(result);
                assertEquals("DEFAULT_BOUNDARY", result.getBoundary());
        }

        @Test
        public void testValidate_GlobalDefaultBoundary() {
                String token = "header.payload.signature";
                String issuer = "https://sts.windows.net/tenant-id/";

                AuthProperties.Provider provider = new AuthProperties.Provider();
                provider.setId("azure");
                provider.setIssuerUri(issuer);
                provider.setJwkSetUri("http://jwks");
                provider.setRoleClaimKey("roles");
                // No provider-specific defaultBoundaryCode
                ReflectionTestUtils.setField(provider, "roleMapping",
                                Collections.singletonMap("AZURE_ROLE_1", "DIGIT_ROLE_1"));

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));
                when(authProperties.getDefaultBoundaryCode()).thenReturn("GLOBAL_DEFAULT_BOUNDARY");

                @SuppressWarnings("unchecked")
                Map<String, JwtDecoder> decoders = (Map<String, JwtDecoder>) ReflectionTestUtils.getField(
                                idpJwtValidator,
                                "decoders");
                decoders.put("azure", jwtDecoder);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuer);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("AZURE_ROLE_1"));
                claims.put("tenantId", "pb.amritsar");
                claims.put("userType", "EMPLOYEE");

                Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600),
                                Collections.singletonMap("alg", "RS256"), claims);

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String realLookingToken = header + "." + payload + ".signature";

                when(jwtDecoder.decode(realLookingToken)).thenReturn(jwt);

                OidcValidatedJwt result = idpJwtValidator.validate(realLookingToken);

                assertNotNull(result);
                assertEquals("GLOBAL_DEFAULT_BOUNDARY", result.getBoundary());
        }

        @Test
        public void testValidate_Success_MDMSFetch() throws Exception {
                String token = "header.payload.signature";
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider = new AuthProperties.Provider();
                provider.setId("azure");
                provider.setIssuerUri(issuer);
                provider.setJwkSetUri("http://jwks");
                provider.setRoleClaimKey("roles");
                provider.setProjectName("NMCP");
                provider.setHierarchyType("REVENUE");
                provider.setDefaultBoundaryCode("COUNTRY_CODE_TOGO");

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                @SuppressWarnings("unchecked")
                Map<String, JwtDecoder> decoders = (Map<String, JwtDecoder>) ReflectionTestUtils.getField(
                                idpJwtValidator,
                                "decoders");
                decoders.put("azure", jwtDecoder);

                // Mock MDMS response
                String jsonResponse = "{\"MdmsRes\":{\"SSO-ROLE-MAPPING\":{\"sso-to-digit-roles\":[{\"ssoRole\":\"AZURE_ROLE_1\",\"digitRole\":\"DIGIT_ROLE_1\"}]}}}";
                JsonNode node = new ObjectMapper().readTree(jsonResponse);

                ReflectionTestUtils.setField(idpJwtValidator, "mdmsHost", "http://mdms");
                ReflectionTestUtils.setField(idpJwtValidator, "mdmsEndpoint", "/search");
                ReflectionTestUtils.setField(idpJwtValidator, "ssoRoleMasterName", "sso-to-digit-roles");
                ReflectionTestUtils.setField(idpJwtValidator, "ssoRoleModuleName", "SSO-ROLE-MAPPING");

                when(restTemplate.postForObject(org.mockito.Matchers.anyString(), org.mockito.Matchers.any(),
                                org.mockito.Matchers.eq(JsonNode.class)))
                                .thenReturn(node);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuer);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("AZURE_ROLE_1"));
                claims.put("tenantId", tenantId);
                claims.put("userType", "EMPLOYEE");

                Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600),
                                Collections.singletonMap("alg", "RS256"), claims);

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String realLookingToken = header + "." + payload + ".signature";

                when(jwtDecoder.decode(realLookingToken)).thenReturn(jwt);

                OidcValidatedJwt result = idpJwtValidator.validate(realLookingToken);

                assertNotNull(result);
                assertEquals(1, result.getRoles().size());
                assertEquals("DIGIT_ROLE_1", result.getRoles().iterator().next());
        }

        @Test
        public void testValidate_FallbackToConfig() {
                String token = "header.payload.signature";
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider = new AuthProperties.Provider();
                provider.setId("azure");
                provider.setIssuerUri(issuer);
                provider.setJwkSetUri("http://jwks");
                provider.setRoleClaimKey("roles");
                provider.setDefaultBoundaryCode("COUNTRY_CODE_TOGO");
                Map<String, String> roleMapping = Collections.singletonMap("AZURE_ROLE_1", "FALLBACK_ROLE");
                ReflectionTestUtils.setField(provider, "roleMapping", roleMapping);

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                @SuppressWarnings("unchecked")
                Map<String, JwtDecoder> decoders = (Map<String, JwtDecoder>) ReflectionTestUtils.getField(
                                idpJwtValidator,
                                "decoders");
                decoders.put("azure", jwtDecoder);

                // Simulate MDMS failure (throwing exception)
                when(restTemplate.postForObject(org.mockito.Matchers.anyString(), org.mockito.Matchers.any(),
                                org.mockito.Matchers.eq(JsonNode.class)))
                                .thenThrow(new RuntimeException("MDMS Down"));

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", issuer);
                claims.put("sub", "user-guid");
                claims.put("roles", Collections.singletonList("AZURE_ROLE_1"));
                claims.put("tenantId", tenantId);
                claims.put("userType", "EMPLOYEE");

                Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600),
                                Collections.singletonMap("alg", "RS256"), claims);

                String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
                String payload = "eyJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC90ZW5hbnQtaWQvIn0";
                String realLookingToken = header + "." + payload + ".signature";

                when(jwtDecoder.decode(realLookingToken)).thenReturn(jwt);

                OidcValidatedJwt result = idpJwtValidator.validate(realLookingToken);

                assertNotNull(result);
                assertEquals(1, result.getRoles().size());
                assertEquals("FALLBACK_ROLE", result.getRoles().iterator().next());
        }

        @Test
        public void testInit_PreloadsCache() throws Exception {
                AuthProperties.Oidc oidc = new AuthProperties.Oidc();
                oidc.setEnabled(true);
                when(authProperties.getOidc()).thenReturn(oidc);

                AuthProperties.Provider provider = new AuthProperties.Provider();
                provider.setId("azure");
                provider.setTenantId("pb.amritsar");
                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                // Mock MDMS response
                String jsonResponse = "{\"MdmsRes\":{\"SSO-ROLE-MAPPING\":{\"sso-to-digit-roles\":[{\"ssoRole\":\"AZURE_ROLE_1\",\"digitRole\":\"DIGIT_ROLE_1\"}]}}}";
                JsonNode node = new ObjectMapper().readTree(jsonResponse);

                ReflectionTestUtils.setField(idpJwtValidator, "mdmsHost", "http://mdms");
                ReflectionTestUtils.setField(idpJwtValidator, "mdmsEndpoint", "/search");
                ReflectionTestUtils.setField(idpJwtValidator, "ssoRoleMasterName", "sso-to-digit-roles");
                ReflectionTestUtils.setField(idpJwtValidator, "ssoRoleModuleName", "SSO-ROLE-MAPPING");

                when(restTemplate.postForObject(any(String.class), any(), eq(JsonNode.class)))
                                .thenReturn(node);

                // Call init
                idpJwtValidator.init();

                // Verify MDMS was called
                verify(restTemplate, times(1)).postForObject(any(String.class), any(), eq(JsonNode.class));

                // Verify cache is populated
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> roleMappingCache = (Map<String, Map<String, String>>) ReflectionTestUtils
                                .getField(idpJwtValidator, "roleMappingCache");
                assertNotNull(roleMappingCache.get("azure:pb.amritsar"));
                assertEquals("DIGIT_ROLE_1", roleMappingCache.get("azure:pb.amritsar").get("AZURE_ROLE_1"));
        }

        @Test
        public void testExtractRoles_MultiRoleSupport() {
                AuthProperties.Provider provider = new AuthProperties.Provider();
                provider.setId("azure");
                provider.setRoleClaimKey("roles");

                Map<String, String> roleMapping = new HashMap<>();
                roleMapping.put("AZURE_ADMIN", "SYSTEM_ADMINISTRATOR,SUPERUSER");
                roleMapping.put("AZURE_USER", "CITIZEN");

                // Manually inject role mapping into the cache
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> roleMappingCache = (Map<String, Map<String, String>>) ReflectionTestUtils
                                .getField(idpJwtValidator, "roleMappingCache");
                roleMappingCache.put("azure:pb.amritsar", roleMapping);

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
        public void testValidate_ArrayNodeMapping() throws Exception {
                String issuer = "https://sts.windows.net/tenant-id/";
                String tenantId = "pb.amritsar";

                AuthProperties.Provider provider = new AuthProperties.Provider();
                provider.setId("azure");
                provider.setIssuerUri(issuer);
                provider.setRoleClaimKey("roles");

                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

                // Mock MDMS response with ArrayNode
                String jsonResponse = "{\"MdmsRes\":{\"SSO-ROLE-MAPPING\":{\"sso-to-digit-roles\":[{\"ssoRole\":\"AZURE_ADMIN\",\"digitRoles\":[\"ROLE1\",\"ROLE2\"]}]}}}";
                JsonNode node = new ObjectMapper().readTree(jsonResponse);

                ReflectionTestUtils.setField(idpJwtValidator, "mdmsHost", "http://mdms");
                ReflectionTestUtils.setField(idpJwtValidator, "mdmsEndpoint", "/search");
                ReflectionTestUtils.setField(idpJwtValidator, "ssoRoleMasterName", "sso-to-digit-roles");
                ReflectionTestUtils.setField(idpJwtValidator, "ssoRoleModuleName", "SSO-ROLE-MAPPING");

                when(restTemplate.postForObject(any(String.class), any(), eq(JsonNode.class)))
                                .thenReturn(node);

                Map<String, Object> claims = new HashMap<>();
                claims.put("tenantId", tenantId);
                claims.put("roles", Collections.singletonList("AZURE_ADMIN"));

                Set<String> roles = (Set<String>) ReflectionTestUtils.invokeMethod(idpJwtValidator, "extractRoles",
                                provider, claims);

                assertEquals(2, roles.size());
                org.junit.Assert.assertTrue(roles.contains("ROLE1"));
                org.junit.Assert.assertTrue(roles.contains("ROLE2"));
        }
}
