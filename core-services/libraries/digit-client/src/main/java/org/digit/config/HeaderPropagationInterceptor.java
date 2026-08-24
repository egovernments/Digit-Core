package org.digit.config;

import java.io.IOException;
import java.util.Map;
import org.digit.util.DigitContextHolder;
import org.digit.util.DigitRequestContext;
import org.digit.util.HeaderStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Supplies tenant, user and correlation headers on outbound calls.
 *
 * <p>Resolution order, highest precedence first:
 * <ol>
 *   <li>headers already set on the request — a client that set one explicitly is never overridden;</li>
 *   <li>an explicit {@link DigitRequestContext} from {@link DigitContextHolder}, for calls made
 *       outside a servlet request;</li>
 *   <li>the inbound servlet request, filtered by {@link PropagationProperties}.</li>
 * </ol>
 */
public class HeaderPropagationInterceptor
implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(HeaderPropagationInterceptor.class);
    private final PropagationProperties propagationProperties;

    public HeaderPropagationInterceptor(PropagationProperties propagationProperties) {
        this.propagationProperties = propagationProperties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {
            HttpHeaders headers = request.getHeaders();
            DigitRequestContext context = DigitContextHolder.get();
            if (context != null) {
                // Not filtered by the allow list: that list governs which *inbound* headers may be
                // forwarded onwards, whereas this context was constructed deliberately by the caller.
                applyIfAbsent(headers, context.toHeaders(), false);
            } else {
                applyIfAbsent(headers, HeaderStore.getHeadersToPropagate(this.propagationProperties), true);
            }
        }
        catch (Exception e) {
            // A failure here means the call goes out without context headers and the service
            // rejects it; log the cause rather than letting it surface as an opaque 400.
            log.error("Failed to resolve DIGIT context headers for {} {}", request.getMethod(), request.getURI(), e);
        }
        return execution.execute(request, body);
    }

    /**
     * Adds the resolved headers, skipping any the caller already set. Uses {@code set} rather than
     * {@code add} so a re-used request or a doubly registered interceptor cannot produce a header
     * with two values, which the gateway and the services do not expect.
     */
    private void applyIfAbsent(HttpHeaders headers, Map<String, String> resolved, boolean applyAllowList) {
        if (resolved == null || resolved.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : resolved.entrySet()) {
            String name = entry.getKey();
            if (applyAllowList && !this.propagationProperties.shouldPropagate(name)) {
                continue;
            }
            if (headers.containsHeader(name)) {
                continue;
            }
            headers.set(name, entry.getValue());
        }
        if (log.isDebugEnabled()) {
            log.debug("Applied DIGIT context headers: {}", resolved.keySet());
        }
    }
}
