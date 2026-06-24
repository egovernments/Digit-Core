package org.digit.tracer.http;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.digit.tracer.config.TracerProperties;
import org.digit.tracer.observability.ObservabilityMetrics;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Servlet filter mirroring the Go observability/middleware.go.
 * Records per-request metrics and logs URI + response status.
 * OTel span creation is handled automatically by the OpenTelemetry Spring Boot starter.
 */
public class RequestTracingFilter implements Filter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestTracingFilter.class);

    private final TracerProperties properties;
    private final ObservabilityMetrics metrics;
    private final Pattern skipPattern;

    public RequestTracingFilter(TracerProperties properties, ObservabilityMetrics metrics) {
        this.properties  = properties;
        this.metrics     = metrics;
        this.skipPattern = properties.filterSkipPattern() != null
            ? Pattern.compile(properties.filterSkipPattern())
            : null;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (shouldSkip(req)) {
            chain.doFilter(request, response);
            return;
        }

        Instant start = Instant.now();
        String  path  = req.getRequestURI();
        String  method = req.getMethod();

        // Enrich the active OTel span with tenant context if present
        String tenantId = req.getHeader("tenantId");
        if (tenantId != null) {
            Span.current().setAttribute("tenant.id", tenantId);
        }

        if (properties.requestLoggingEnabled()) {
            log.info("Received request: {} {}", method, path);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            int      status   = res.getStatus();
            Duration duration = Duration.between(start, Instant.now());
            metrics.recordHttpRequest(method, path, status, duration);
            log.info("Completed {} {} -> {}", method, path, status);
        }
    }

    private boolean shouldSkip(HttpServletRequest req) {
        if (skipPattern == null) return false;
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        return skipPattern.matcher(uri).matches();
    }
}
