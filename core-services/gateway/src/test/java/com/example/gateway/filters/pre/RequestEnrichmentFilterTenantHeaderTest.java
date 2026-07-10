package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static com.example.gateway.constants.GatewayConstants.REQUEST_TENANT_ID_KEY;
import static com.example.gateway.constants.GatewayConstants.TENANTID_MDC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The resolved tenantId must be attached to the request that is actually forwarded downstream — done by
 * rebuilding the exchange (a bare getRequest().mutate() inside the body-rewrite is discarded by SCG).
 * These verify the header lands on the exchange passed to the next filter, from the attribute or the
 * ?tenantId query fallback, and is gated by the propagation flag.
 */
class RequestEnrichmentFilterTenantHeaderTest {

    private RequestEnrichmentFilter filter(boolean flag) {
        ApplicationProperties props = new ApplicationProperties();
        props.setTenantPropagationEnabled(flag);
        return new RequestEnrichmentFilter(null, null, new CommonUtils(new ObjectMapper(), null, null), props);
    }

    private String forwardedTenant(RequestEnrichmentFilter f, MockServerWebExchange exchange) {
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = ex -> { captured.set(ex); return Mono.empty(); };
        f.filter(exchange, chain).block();
        return captured.get().getRequest().getHeaders().getFirst(REQUEST_TENANT_ID_KEY);
    }

    @Test
    void tenantHeaderForwarded_fromQueryParam() {
        MockServerWebExchange ex = MockServerWebExchange.from(MockServerHttpRequest.get("/x/_search?tenantId=dev").build());
        assertEquals("dev", forwardedTenant(filter(true), ex));
    }

    @Test
    void tenantHeaderForwarded_fromExchangeAttribute() {
        MockServerWebExchange ex = MockServerWebExchange.from(MockServerHttpRequest.get("/x/_search").build());
        ex.getAttributes().put(TENANTID_MDC, "dev.city");
        assertEquals("dev.city", forwardedTenant(filter(true), ex));
    }

    @Test
    void noTenantHeader_whenPropagationDisabled() {
        MockServerWebExchange ex = MockServerWebExchange.from(MockServerHttpRequest.get("/x/_search?tenantId=dev").build());
        assertNull(forwardedTenant(filter(false), ex));
    }
}
