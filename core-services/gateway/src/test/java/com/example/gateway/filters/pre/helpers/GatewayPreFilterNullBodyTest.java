package com.example.gateway.filters.pre.helpers;

import com.example.gateway.config.ApplicationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.RBAC_BOOLEAN_FLAG_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Empty-body (e.g. GET) requests reach the secured-path pre-filter helpers with a null body/map.
 * Before the null-body guards, each of these threw a raw NullPointerException (HTTP 500). These tests
 * confirm every helper now handles a null body safely: pass through where there is nothing to do, and
 * REJECT (never silently allow) where user/auth context is required. No helper may return an NPE.
 */
class GatewayPreFilterNullBodyTest {

    private static final String PROTECTED_PATH = "/some-service/v1/_search";
    private static final String OPEN_PATH = "/open-service/v1/_search";

    private ApplicationProperties propsWithOpen(String... openPaths) {
        ApplicationProperties props = new ApplicationProperties();
        props.setOpenEndpointsWhitelistValues(List.of(openPaths));
        return props;
    }

    private MockServerWebExchange getExchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }

    // ---------------- RbacPreCheckFilterHelper ----------------

    @Test
    void rbacPreCheck_nullMap_protected_setsRbacTrue_noNpe() {
        RbacPreCheckFilterHelper helper =
                new RbacPreCheckFilterHelper(Collections.emptyList(), propsWithOpen());
        MockServerWebExchange exchange = getExchange(PROTECTED_PATH);

        Object result = Mono.from(helper.apply(exchange, null)).block();

        assertNull(result); // Mono.empty() -> null, no NPE
        assertEquals(true, exchange.getAttributes().get(RBAC_BOOLEAN_FLAG_NAME));
    }

    @Test
    void rbacPreCheck_nullMap_open_setsRbacFalse_noNpe() {
        RbacPreCheckFilterHelper helper =
                new RbacPreCheckFilterHelper(Collections.emptyList(), propsWithOpen(OPEN_PATH));
        MockServerWebExchange exchange = getExchange(OPEN_PATH);

        Object result = Mono.from(helper.apply(exchange, null)).block();

        assertNull(result);
        assertEquals(false, exchange.getAttributes().get(RBAC_BOOLEAN_FLAG_NAME));
    }

    @Test
    void rbacPreCheck_nonNullMap_passesBodyThrough() {
        RbacPreCheckFilterHelper helper =
                new RbacPreCheckFilterHelper(Collections.emptyList(), propsWithOpen());
        MockServerWebExchange exchange = getExchange(PROTECTED_PATH);
        Map<String, Object> body = new HashMap<>();

        Object result = Mono.from(helper.apply(exchange, body)).block();

        assertSame(body, result); // non-null body is preserved unchanged
        assertEquals(true, exchange.getAttributes().get(RBAC_BOOLEAN_FLAG_NAME));
    }

    // ---------------- RbacPreCheckFormDataFilterHelper ----------------

    @Test
    void rbacPreCheckFormData_nullBody_protected_setsRbacTrue_noNpe() {
        RbacPreCheckFormDataFilterHelper helper =
                new RbacPreCheckFormDataFilterHelper(Collections.emptyList(), propsWithOpen());
        MockServerWebExchange exchange = getExchange(PROTECTED_PATH);

        Object result = Mono.from(helper.apply(exchange, null)).block();

        assertNull(result);
        assertEquals(true, exchange.getAttributes().get(RBAC_BOOLEAN_FLAG_NAME));
    }

    @Test
    void rbacPreCheckFormData_nonNullBody_passesThrough() {
        RbacPreCheckFormDataFilterHelper helper =
                new RbacPreCheckFormDataFilterHelper(Collections.emptyList(), propsWithOpen());
        MockServerWebExchange exchange = getExchange(PROTECTED_PATH);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        Object result = Mono.from(helper.apply(exchange, body)).block();

        assertSame(body, result);
    }

    // ---------------- RbacFilterHelper ----------------

    @Test
    void rbacFilter_nullMap_rejects_withoutNpe_andWithoutBypass() {
        RbacFilterHelper helper =
                new RbacFilterHelper(new ObjectMapper(), null, null, null, null);
        MockServerWebExchange exchange = getExchange(PROTECTED_PATH);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> Mono.from(helper.apply(exchange, null)).block());

        // must be a deliberate rejection, NOT an NPE, and never a silent pass-through
        assertFalse(ex instanceof NullPointerException, "must not surface an NPE");
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("User information not found"));
    }

    // ---------------- AuthCheckFilterHelper ----------------

    @Test
    void authCheck_nullBody_passesThrough_withoutNpe() {
        AuthCheckFilterHelper helper = new AuthCheckFilterHelper(new ObjectMapper(), null);
        MockServerWebExchange exchange = getExchange(PROTECTED_PATH);

        Object result = Mono.from(helper.apply(exchange, null)).block();

        assertNull(result); // Mono.empty() -> null; no NPE, no AUTHENTICATION_ERROR
    }
}
