package com.example.gateway.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.HashMap;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.TENANTID_MDC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tenant propagation for tracing on non-central-instance envs. resolveTenantForTracing must put the
 * resolved tenantId into the exchange attribute (which RequestEnrichmentFilter then forwards to the
 * downstream service), resolve nothing gracefully when absent, and never override an already-resolved
 * tenantId. It must never throw — tracing must not break a request.
 */
class CommonUtilsResolveTenantForTracingTest {

    // resolveTenantForTracing's GET/query-param path uses only ObjectMapper; the other deps are unused here.
    private final CommonUtils commonUtils = new CommonUtils(new ObjectMapper(), null, null);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    private MockServerWebExchange get(String uri) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(uri).build());
    }

    @Test
    void getWithTenantQueryParam_setsTenantAttribute() {
        MockServerWebExchange exchange = get("/some/secured/_search?tenantId=dev");
        commonUtils.resolveTenantForTracing(exchange, null);
        assertEquals("dev", exchange.getAttributes().get(TENANTID_MDC));
    }

    @Test
    void getWithoutTenant_setsNothing_andDoesNotThrow() {
        MockServerWebExchange exchange = get("/some/secured/_search");
        assertDoesNotThrow(() -> commonUtils.resolveTenantForTracing(exchange, null));
        assertNull(exchange.getAttributes().get(TENANTID_MDC));
    }

    @Test
    void alreadyResolvedTenant_isNotOverwritten() {
        MockServerWebExchange exchange = get("/some/secured/_search?tenantId=dev");
        exchange.getAttributes().put(TENANTID_MDC, "pre-set");
        commonUtils.resolveTenantForTracing(exchange, null);
        assertEquals("pre-set", exchange.getAttributes().get(TENANTID_MDC));
    }

    @Test
    void commaSeparatedTenants_picksMostSpecific() {
        MockServerWebExchange exchange = get("/some/secured/_search?tenantId=dev,dev.city");
        commonUtils.resolveTenantForTracing(exchange, null);
        assertEquals("dev.city", exchange.getAttributes().get(TENANTID_MDC));
    }

    // POST-body path: tenantId lives in the JSON body (not a query param) and must land in the attribute
    @Test
    void postWithTenantInBody_setsTenantAttribute() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/some/secured/_create")
                        .header("Content-Type", "application/json").build());
        Map<String, Object> individual = new HashMap<>();
        individual.put("tenantId", "dev");
        Map<String, Object> body = new HashMap<>();
        body.put("Individual", individual);

        commonUtils.resolveTenantForTracing(exchange, body);

        assertEquals("dev", exchange.getAttributes().get(TENANTID_MDC));
    }
}
