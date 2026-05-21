package org.digit.interceptor;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OpenTelemetryRestTemplateInterceptor
implements ClientHttpRequestInterceptor {
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;
    private static final TextMapSetter<HttpRequest> SETTER = (carrier, key, value) -> {
        if (carrier != null && key != null && value != null) {
            carrier.getHeaders().set(key, value);
        }
    };

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        URI uri = request.getURI();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        Span span = this.tracer.spanBuilder(method + " " + uri.getPath()).setSpanKind(SpanKind.CLIENT).startSpan();
        span.setAttribute("http.method", method);
        span.setAttribute("http.url", uri.toString());
        span.setAttribute("http.scheme", uri.getScheme());
        span.setAttribute("http.host", uri.getHost());
        span.setAttribute("http.target", uri.getPath());
        if (uri.getPort() > 0) {
            span.setAttribute("net.peer.port", (long)uri.getPort());
        }
        if (body != null && body.length > 0) {
            span.setAttribute("http.request.body.size", (long)body.length);
        }
        try {
            ClientHttpResponse clientHttpResponse;
            block15: {
                Scope scope = span.makeCurrent();
                try {
                    this.openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), request, SETTER);
                    log.debug("Starting HTTP trace: {} {}", (Object)method, (Object)uri);
                    ClientHttpResponse response = execution.execute(request, body);
                    int statusCode = response.getStatusCode().value();
                    span.setAttribute("http.status_code", (long)statusCode);
                    if (statusCode >= 400) {
                        span.setStatus(StatusCode.ERROR, "HTTP " + statusCode);
                    } else {
                        span.setStatus(StatusCode.OK);
                    }
                    log.debug("Completed HTTP trace: {} {} - Status: {}", new Object[]{method, uri, statusCode});
                    clientHttpResponse = response;
                    if (scope == null) break block15;
                }
                catch (Throwable throwable) {
                    try {
                        if (scope != null) {
                            try {
                                scope.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (Exception e) {
                        span.recordException((Throwable)e);
                        span.setStatus(StatusCode.ERROR, e.getMessage());
                        log.error("Error in HTTP trace: {} {}", new Object[]{method, uri, e});
                        throw e;
                    }
                }
                scope.close();
            }
            return clientHttpResponse;
        }
        finally {
            span.end();
        }
    }
}