package org.egov.user.security;

import org.egov.user.security.oauth2.custom.jwt.JwtConstants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CustomAuthenticationKeyGeneratorTest {

    private final CustomAuthenticationKeyGenerator generator = new CustomAuthenticationKeyGenerator();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(generator, "hashAlgorithm", "MD5");
    }

    private OAuth2Authentication authentication(String grantType, Map<String, Serializable> extensions) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", grantType);
        params.put("tenantId", "bo");
        OAuth2Request request = new OAuth2Request(params, "egov-user-client", null, true,
                Collections.singleton("read"), null, null, null, extensions);
        return new OAuth2Authentication(request, new UsernamePasswordAuthenticationToken("alice", "n/a"));
    }

    @Test
    public void direct_and_sso_logins_of_same_user_get_different_keys() {
        Map<String, Serializable> sso = new HashMap<>();
        sso.put(JwtConstants.EXT_IDP_PROVIDER_ID, "keycloak");

        String direct = generator.extractKey(authentication("password", null));
        String viaIdp = generator.extractKey(authentication(JwtConstants.GRANT_TYPE_JWT_EXCHANGE, sso));

        assertNotEquals(direct, viaIdp);
    }

    @Test
    public void direct_login_key_ignores_grant_type_and_is_unchanged() {
        assertEquals(generator.extractKey(authentication("password", null)),
                generator.extractKey(authentication("refresh_token", new HashMap<>())));
    }
}
