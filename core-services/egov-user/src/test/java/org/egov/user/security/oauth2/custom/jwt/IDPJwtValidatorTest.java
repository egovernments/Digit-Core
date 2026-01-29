package org.egov.user.security.oauth2.custom.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.user.config.AuthProperties;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IDPJwtValidatorTest {

    @Mock
    private AuthProperties authProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JwtDecoder jwtDecoder;

    private IDPJwtValidator idpJwtValidator;

    @Before
    public void setup() {
        idpJwtValidator = new IDPJwtValidator(authProperties, objectMapper);
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
        Map<String, String> boundaryMapping = new HashMap<>();
        boundaryMapping.put("AZURE_ROLE_1", "COUNTRY_CODE_TOGO");
        ReflectionTestUtils.setField(provider, "roleBoundaryMapping", boundaryMapping);

        when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));

        // Inject mocked decoder into the decoders map
        Map<String, JwtDecoder> decoders = (Map<String, JwtDecoder>) ReflectionTestUtils.getField(idpJwtValidator,
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
        ReflectionTestUtils.setField(provider, "roleMapping", Collections.singletonMap("AZURE_ROLE_1", "DIGIT_ROLE_1"));

        when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));

        Map<String, JwtDecoder> decoders = (Map<String, JwtDecoder>) ReflectionTestUtils.getField(idpJwtValidator,
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
        ReflectionTestUtils.setField(provider, "roleMapping", Collections.singletonMap("AZURE_ROLE_1", "DIGIT_ROLE_1"));

        when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
        when(authProperties.getDefaultBoundaryCode()).thenReturn("GLOBAL_DEFAULT_BOUNDARY");

        Map<String, JwtDecoder> decoders = (Map<String, JwtDecoder>) ReflectionTestUtils.getField(idpJwtValidator,
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
}
