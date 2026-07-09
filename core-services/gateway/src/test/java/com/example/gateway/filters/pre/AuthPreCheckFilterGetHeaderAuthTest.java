package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static com.example.gateway.constants.GatewayConstants.AUTH_BOOLEAN_FLAG_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Anti-regression: a secured GET carrying the auth token in the "auth-token" HEADER must authenticate
 * (AUTH flag TRUE) via handleAuthPreCheck — NOT return 401. The master-lineage gateway (image 9feff20)
 * had dropped this GET header-auth path and 401'd such requests (e.g. DIGIT Studio
 * GET /public-service-init/v1/service?tenantId=dev). This base (gateway-2.9.2-base-docker-upgrade)
 * keeps it, and the tenant-propagation changes leave AuthPreCheckFilter untouched.
 */
class AuthPreCheckFilterGetHeaderAuthTest {

    private ApplicationProperties props() {
        ApplicationProperties p = new ApplicationProperties();
        p.setOpenEndpointsWhitelistValues(Collections.emptyList());
        p.setMixModeEndpointListValues(Collections.emptyList());
        return p;
    }

    @Test
    void securedGet_withAuthTokenHeader_authenticates_not401() {
        // token-present path uses only the flag + chain; other deps are unused here
        AuthPreCheckFilter filter = new AuthPreCheckFilter(null, null, props(), null, null, null);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/public-service-init/v1/service?tenantId=dev")
                        .header("auth-token", "valid-session-token").build());
        GatewayFilterChain chain = ex -> Mono.empty();

        // must not throw the 401 CustomException, and must mark the request as authenticated
        assertDoesNotThrow(() -> filter.handleAuthPreCheck(exchange, chain).block());
        assertEquals(Boolean.TRUE, exchange.getAttributes().get(AUTH_BOOLEAN_FLAG_NAME));
    }
}
