package org.egov.user.security.oauth2.custom.jwt;

import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JwtExchangeTokenGranterTest {

    private OAuth2Request provenanceRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", JwtConstants.GRANT_TYPE_JWT_EXCHANGE);
        params.put(JwtConstants.PARAM_TENANT_ID, "pb");
        params.put(JwtConstants.PARAM_ASSERTION, "raw.id.token");
        OAuth2Request request = new OAuth2Request(params, "egov-user-client", null, true,
                Collections.singleton("read"), null, null, null, null);

        Map<String, String> details = new LinkedHashMap<>();
        details.put(JwtConstants.EXT_IDP_PROVIDER_ID, "keycloak");
        details.put(JwtConstants.EXT_IDP_TENANT_ID, "pb");
        details.put(JwtConstants.EXT_IDP_ISSUER, "https://idp/realms/pb");
        details.put(JwtConstants.EXT_IDP_SUBJECT, "sub-1");
        details.put(JwtConstants.PARAM_TENANT_ID, "pb");
        details.put(JwtConstants.PARAM_USER_TYPE, "EMPLOYEE");
        UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken("u", "p");
        authResult.setDetails(details);

        return JwtExchangeTokenGranter.withIdpProvenance(request, authResult);
    }

    @Test
    public void stores_provenance_and_drops_assertion() {
        OAuth2Request stored = provenanceRequest();

        assertFalse(stored.getRequestParameters().containsKey(JwtConstants.PARAM_ASSERTION));
        assertEquals("pb", stored.getRequestParameters().get(JwtConstants.PARAM_TENANT_ID));
        assertEquals(JwtConstants.GRANT_TYPE_JWT_EXCHANGE, stored.getGrantType());
        assertEquals("egov-user-client", stored.getClientId());
        assertEquals("keycloak", stored.getExtensions().get(JwtConstants.EXT_IDP_PROVIDER_ID));
        assertEquals("pb", stored.getExtensions().get(JwtConstants.EXT_IDP_TENANT_ID));
        assertEquals("https://idp/realms/pb", stored.getExtensions().get(JwtConstants.EXT_IDP_ISSUER));
        assertEquals("sub-1", stored.getExtensions().get(JwtConstants.EXT_IDP_SUBJECT));
        assertEquals(4, stored.getExtensions().size());
    }

    @Test
    public void provenance_survives_refresh_grant() {
        OAuth2Request refreshed = provenanceRequest()
                .refresh(new TokenRequest(Collections.emptyMap(), "egov-user-client", null, "refresh_token"));

        assertEquals(JwtConstants.GRANT_TYPE_JWT_EXCHANGE, refreshed.getGrantType());
        assertEquals("keycloak", refreshed.getExtensions().get(JwtConstants.EXT_IDP_PROVIDER_ID));
        assertEquals("sub-1", refreshed.getExtensions().get(JwtConstants.EXT_IDP_SUBJECT));
    }
}
