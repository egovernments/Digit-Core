package org.digit.config;

import java.io.IOException;
import java.net.URI;
import org.digit.util.DigitContextHolder;
import org.digit.util.DigitRequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Which context headers actually reach the service.
 *
 * <p>X-User-ID is the load-bearing one: billing and workflow reject every write without it, and
 * registry rejects every call including reads, so its absence made those clients unusable.
 */
class HeaderPropagationTest {

    private final HeaderPropagationInterceptor interceptor =
            new HeaderPropagationInterceptor(new PropagationProperties());

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        DigitContextHolder.clear();
    }

    @Test
    void forwardsUserIdFromInboundRequest() throws Exception {
        MockHttpServletRequest inbound = new MockHttpServletRequest();
        inbound.addHeader("X-Tenant-ID", "TEST3");
        inbound.addHeader("X-User-ID", "u-1");
        inbound.addHeader("Authorization", "Bearer token-1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inbound));

        HttpHeaders sent = intercept();

        assertEquals("TEST3", sent.getFirst("X-Tenant-ID"));
        assertEquals("u-1", sent.getFirst("X-User-ID"));
        assertEquals("Bearer token-1", sent.getFirst("Authorization"));
    }

    @Test
    void appliesExplicitContextWhenThereIsNoInboundRequest() throws Exception {
        // The async, Kafka-consumer and bootstrap case: nothing to propagate from.
        DigitContextHolder.set(DigitRequestContext.builder()
                .tenantId("TEST3").userId("u-2").clientId("svc-license").authToken("token-2").build());

        HttpHeaders sent = intercept();

        assertEquals("TEST3", sent.getFirst("X-Tenant-ID"));
        assertEquals("u-2", sent.getFirst("X-User-ID"));
        assertEquals("svc-license", sent.getFirst("X-Client-ID"));
        assertEquals("Bearer token-2", sent.getFirst("Authorization"));
    }

    @Test
    void addsBearerPrefixOnlyWhenMissing() throws Exception {
        DigitContextHolder.set(DigitRequestContext.builder().authToken("Bearer already-prefixed").build());
        assertEquals("Bearer already-prefixed", intercept().getFirst("Authorization"));
    }

    @Test
    void explicitContextTakesPrecedenceOverInboundRequest() throws Exception {
        MockHttpServletRequest inbound = new MockHttpServletRequest();
        inbound.addHeader("X-Tenant-ID", "AMBIENT");
        inbound.addHeader("X-User-ID", "ambient-user");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inbound));
        DigitContextHolder.set(DigitRequestContext.of("EXPLICIT", "explicit-user", null));

        HttpHeaders sent = intercept();

        assertEquals("EXPLICIT", sent.getFirst("X-Tenant-ID"));
        assertEquals("explicit-user", sent.getFirst("X-User-ID"));
    }

    @Test
    void neverOverwritesAHeaderTheClientSetItself() throws Exception {
        DigitContextHolder.set(DigitRequestContext.of("TEST3", "u-1", null));

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://svc/v3/x"));
        request.getHeaders().set("X-Tenant-ID", "SET-BY-CALLER");
        interceptor.intercept(request, new byte[0], execution());

        assertEquals("SET-BY-CALLER", request.getHeaders().getFirst("X-Tenant-ID"));
        assertEquals("u-1", request.getHeaders().getFirst("X-User-ID"));
    }

    @Test
    void sendsNothingWhenThereIsNoContextAtAll() throws Exception {
        HttpHeaders sent = intercept();
        assertNull(sent.getFirst("X-Tenant-ID"));
        assertNull(sent.getFirst("X-User-ID"));
    }

    /** Scoped helper must restore whatever context was in place, so pooled threads stay clean. */
    @Test
    void scopedRunRestoresPreviousContext() {
        DigitContextHolder.set(DigitRequestContext.of("OUTER", "outer-user", null));
        DigitContextHolder.run(DigitRequestContext.of("INNER", "inner-user", null),
                () -> assertEquals("INNER", DigitContextHolder.get().getTenantId()));
        assertEquals("OUTER", DigitContextHolder.get().getTenantId());
    }

    private HttpHeaders intercept() throws IOException {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://svc/v3/x"));
        interceptor.intercept(request, new byte[0], execution());
        return request.getHeaders();
    }

    private ClientHttpRequestExecution execution() {
        return new ClientHttpRequestExecution() {
            @Override
            public ClientHttpResponse execute(HttpRequest request, byte[] body) {
                return new MockClientHttpResponse(new byte[0], 200);
            }
        };
    }
}
