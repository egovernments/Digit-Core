package com.digit.interceptor;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * RestTemplate interceptor that adds OpenTelemetry tracing to all HTTP requests.
 * Creates spans for each outbound HTTP call with detailed attributes.
 */
@Slf4j
@RequiredArgsConstructor
public class OpenTelemetryRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    /**
     * TextMapSetter for injecting trace context into HTTP headers
     */
    private static final TextMapSetter<HttpRequest> SETTER = (carrier, key, value) -> {
        if (carrier != null && key != null && value != null) {
            carrier.getHeaders().set(key, value);
        }
    };

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) 
            throws IOException {
        
        URI uri = request.getURI();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        
        // Create span for the HTTP request
        Span span = tracer.spanBuilder(method + " " + uri.getPath())
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

        // Add span attributes
        span.setAttribute("http.method", method);
        span.setAttribute("http.url", uri.toString());
        span.setAttribute("http.scheme", uri.getScheme());
        span.setAttribute("http.host", uri.getHost());
        span.setAttribute("http.target", uri.getPath());
        
        if (uri.getPort() > 0) {
            span.setAttribute("net.peer.port", uri.getPort());
        }
        
        if (body != null && body.length > 0) {
            span.setAttribute("http.request.body.size", body.length);
        }

        try (Scope scope = span.makeCurrent()) {
            // Inject trace context into request headers
            openTelemetry.getPropagators().getTextMapPropagator()
                    .inject(Context.current(), request, SETTER);

            log.debug("Starting HTTP trace: {} {}", method, uri);

            // Execute the request
            ClientHttpResponse response = execution.execute(request, body);

            // Add response attributes
            int statusCode = response.getStatusCode().value();
            span.setAttribute("http.status_code", statusCode);
            
            // Set span status based on HTTP status code
            if (statusCode >= 400) {
                span.setStatus(StatusCode.ERROR, "HTTP " + statusCode);
            } else {
                span.setStatus(StatusCode.OK);
            }

            log.debug("Completed HTTP trace: {} {} - Status: {}", method, uri, statusCode);
            
            return response;
            
        } catch (Exception e) {
            // Record exception in span
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            log.error("Error in HTTP trace: {} {}", method, uri, e);
            throw e;
        } finally {
            // End the span
            span.end();
        }
    }
}
