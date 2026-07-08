package com.example.gateway.filters.pre.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.AUTH_BOOLEAN_FLAG_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Empty-body (e.g. GET) requests reach the pre-filter helpers with a null body. Before the null-body
 * guard, {@link AuthPreCheckFilterHelper} threw a raw NullPointerException at {@code body.get(...)}
 * (HTTP 500). These tests lock in the corrected behaviour: a protected endpoint must reject with 401
 * (no auth bypass), while open / mixed-mode endpoints pass through without an NPE.
 */
class AuthPreCheckFilterHelperNullBodyTest {

    private static final String PROTECTED_PATH = "/some-service/v1/_search";
    private static final String MIXED_PATH = "/mixed-service/v1/_search";
    private static final String OPEN_PATH = "/open-service/v1/_search";

    private AuthPreCheckFilterHelper helper(List<String> open, List<String> mixed) {
        return new AuthPreCheckFilterHelper(open, mixed, new ObjectMapper(), null);
    }

    private MockServerWebExchange exchangeFor(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }

    @Test
    void nullBody_protectedEndpoint_rejectsWith401_andDoesNotBypassAuth() {
        AuthPreCheckFilterHelper helper = helper(Collections.emptyList(), Collections.emptyList());
        MockServerWebExchange exchange = exchangeFor(PROTECTED_PATH);

        CustomException ex = assertThrows(CustomException.class,
                () -> Mono.from(helper.apply(exchange, null)).block());

        assertEquals(HttpStatus.UNAUTHORIZED.toString(), ex.getCode());
        // The auth flag must never be flipped to TRUE for a null-body protected request.
        assertNotEquals(Boolean.TRUE, exchange.getAttributes().get(AUTH_BOOLEAN_FLAG_NAME));
    }

    @Test
    void nullBody_mixedModeEndpoint_routesAnonymously_withoutNpe() {
        AuthPreCheckFilterHelper helper = helper(Collections.emptyList(), Collections.singletonList(MIXED_PATH));
        MockServerWebExchange exchange = exchangeFor(MIXED_PATH);

        Map result = Mono.from(helper.apply(exchange, null)).block();

        assertNull(result); // Mono.empty() -> null, i.e. no NPE and no fabricated body
        assertEquals(Boolean.FALSE, exchange.getAttributes().get(AUTH_BOOLEAN_FLAG_NAME));
    }

    @Test
    void nullBody_openEndpoint_passesThrough_withoutNpe() {
        AuthPreCheckFilterHelper helper = helper(Collections.singletonList(OPEN_PATH), Collections.emptyList());
        MockServerWebExchange exchange = exchangeFor(OPEN_PATH);

        Map result = Mono.from(helper.apply(exchange, null)).block();

        assertNull(result);
        assertEquals(Boolean.FALSE, exchange.getAttributes().get(AUTH_BOOLEAN_FLAG_NAME));
    }
}
