package org.egov.user.security.oauth2.custom.jwt;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Inline JWKS ("test mode"): a provider may carry its verification key by value for IdPs
 * this service cannot reach. Every other check — signature, expiry, issuer, audience —
 * must behave exactly as it does for a fetched JWKS.
 */
@RunWith(MockitoJUnitRunner.class)
public class IDPJwtValidatorInlineJwkSetTest {

    private static final String ISSUER = "https://sts.windows.net/test-tenant/";
    private static final String AUDIENCE = "test-audience";
    private static final String TENANT = "chaduat";

    @Mock
    private AuthProperties authProperties;

    @Mock
    private OidcProviderSupplier oidcProviderSupplier;

    private IDPJwtValidator validator;
    private String publicJwkSet;

    @Before
    public void setup() {
        validator = new IDPJwtValidator(authProperties, oidcProviderSupplier);
        publicJwkSet = JwtTestUtils.getTestJWKSet().toPublicJWKSet().toString();
    }

    private void withTestMode(boolean enabled, String... tenants) {
        withConfiguredKey(enabled, null, tenants);
    }

    private void withConfiguredKey(boolean enabled, String configuredJwkSet, String... tenants) {
        AuthProperties.Oidc oidc = new AuthProperties.Oidc();
        AuthProperties.TestMode testMode = new AuthProperties.TestMode();
        testMode.setEnabled(enabled);
        testMode.setTenants(Arrays.asList(tenants));
        testMode.setJwkSet(configuredJwkSet);
        oidc.setTestMode(testMode);
        when(authProperties.getOidc()).thenReturn(oidc);
    }

    private void withProvider(String jwkSet, String jwkSetUri) {
        AuthProperties.Provider provider = AuthProperties.Provider.builder()
                .id("keycloak-local")
                .issuerUri(ISSUER)
                .jwkSet(jwkSet)
                .jwkSetUri(jwkSetUri)
                .tenantId(TENANT)
                .roleClaimKey("roles")
                .audiences(Collections.singletonList(AUDIENCE))
                .build();
        when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));
    }

    private Map<String, Object> claims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", TENANT);
        claims.put("userType", "EMPLOYEE");
        claims.put("preferred_username", "fe.bob");
        return claims;
    }

    @Test
    public void inlineJwkSetVerifiesTokenWithoutAnyJwksUri() throws Exception {
        withTestMode(true, TENANT);
        withProvider(publicJwkSet, null);

        OidcValidatedJwt validated = validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT);

        assertNotNull(validated);
    }

    @Test
    public void inlineJwkSetTakesPrecedenceOverAnUnreachableUri() throws Exception {
        withTestMode(true, TENANT);
        withProvider(publicJwkSet, "http://localhost:1/does-not-exist/certs");

        assertNotNull(validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT));
    }

    @Test
    public void keyConfiguredInApplicationPropertiesIsUsedWhenTheProviderCarriesNone() throws Exception {
        withConfiguredKey(true, publicJwkSet, TENANT);
        withProvider(null, null);

        assertNotNull(validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT));
    }

    @Test
    public void providerLevelKeyWinsOverTheConfiguredOne() throws Exception {
        withConfiguredKey(true, JwtTestUtils.getWrongJWKSet().toPublicJWKSet().toString(), TENANT);
        withProvider(publicJwkSet, null);

        assertNotNull(validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT));
    }

    @Test
    public void configuredKeyIsIgnoredForTenantsOutsideTheAllowList() throws Exception {
        withConfiguredKey(true, publicJwkSet, "some-other-tenant");
        withProvider(null, null);

        try {
            validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT);
            fail("Expected a provider with no reachable JWKS to be refused outside the allow-list");
        } catch (OidcProviderConfigException e) {
            assertEquals(SsoErrorCodes.OIDC_JWKS_MISSING, e.getErrorCode());
        }
    }

    @Test
    public void aProviderWithItsOwnJwksUriKeepsFetchingEvenInTestMode() throws Exception {
        // an existing Microsoft/Azure entry in a tenant that has test mode switched on:
        // it must keep using its own endpoint, never the pinned test-mode key
        withConfiguredKey(true, publicJwkSet, TENANT);
        withProvider(null, "http://localhost:1/unreachable/certs");

        try {
            validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT);
            fail("Expected the provider to use its own jwkSetUri rather than the test-mode key");
        } catch (IdpJwtValidationException e) {
            assertNotNull(e.getErrorCode());
        }
    }

    @Test
    public void inlineJwkSetIsRejectedWhenTestModeIsOff() throws Exception {
        withTestMode(false, TENANT);
        withProvider(publicJwkSet, null);

        try {
            validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT);
            fail("Expected the inline jwkSet to be refused while test mode is off");
        } catch (OidcProviderConfigException e) {
            assertEquals(SsoErrorCodes.OIDC_JWKS_INLINE_NOT_ALLOWED, e.getErrorCode());
        }
    }

    @Test
    public void inlineJwkSetIsRejectedWhenTenantIsNotAllowListed() throws Exception {
        withTestMode(true, "some-other-tenant");
        withProvider(publicJwkSet, null);

        try {
            validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT);
            fail("Expected the inline jwkSet to be refused for a tenant outside the allow-list");
        } catch (OidcProviderConfigException e) {
            assertEquals(SsoErrorCodes.OIDC_JWKS_INLINE_NOT_ALLOWED, e.getErrorCode());
        }
    }

    @Test
    public void unparseableInlineJwkSetIsReported() throws Exception {
        withTestMode(true, TENANT);
        withProvider("{ not a jwks", null);

        try {
            validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT);
            fail("Expected an unparseable inline jwkSet to be reported");
        } catch (OidcProviderConfigException e) {
            assertEquals(SsoErrorCodes.OIDC_JWKS_INLINE_INVALID, e.getErrorCode());
        }
    }

    @Test
    public void emptyInlineJwkSetIsReported() throws Exception {
        withTestMode(true, TENANT);
        withProvider("{\"keys\":[]}", null);

        try {
            validator.validate(JwtTestUtils.createValidSignedJWT(claims()), TENANT);
            fail("Expected an empty inline jwkSet to be reported");
        } catch (OidcProviderConfigException e) {
            assertEquals(SsoErrorCodes.OIDC_JWKS_INLINE_INVALID, e.getErrorCode());
        }
    }

    @Test
    public void signatureIsStillVerifiedAgainstTheInlineKey() throws Exception {
        withTestMode(true, TENANT);
        withProvider(publicJwkSet, null);

        try {
            validator.validate(JwtTestUtils.createJWTWithWrongSignature(claims()), TENANT);
            fail("Expected a token signed with another key to be rejected");
        } catch (IdpJwtValidationException e) {
            assertNotNull(e.getErrorCode());
        }
    }

    @Test
    public void expiryIsStillEnforcedWithAnInlineKey() throws Exception {
        withTestMode(true, TENANT);
        withProvider(publicJwkSet, null);

        try {
            validator.validate(JwtTestUtils.createExpiredJWT(claims()), TENANT);
            fail("Expected an expired token to be rejected");
        } catch (IdpJwtValidationException e) {
            assertNotNull(e.getErrorCode());
        }
    }

    @Test
    public void unsignedTokenIsStillRejectedWithAnInlineKey() throws Exception {
        withTestMode(true, TENANT);
        withProvider(publicJwkSet, null);

        try {
            validator.validate(JwtTestUtils.createUnsignedJWT(claims()), TENANT);
            fail("Expected an alg=none token to be rejected");
        } catch (IdpJwtValidationException e) {
            assertNotNull(e.getErrorCode());
        }
    }
}
