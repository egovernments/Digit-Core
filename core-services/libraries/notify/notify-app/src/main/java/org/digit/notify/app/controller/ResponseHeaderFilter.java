package org.digit.notify.app.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class ResponseHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String requestId = Optional.ofNullable(request.getHeader("X-Request-ID"))
            .orElse(UUID.randomUUID().toString());
        String correlationId = request.getHeader("X-Correlation-ID");
        String tenantId = request.getHeader("X-Tenant-ID");

        request.setAttribute("X-Request-ID", requestId);
        MDC.put("requestId", requestId);
        MDC.put("tenantId", tenantId != null ? tenantId : "");

        class TimingWrapper extends HttpServletResponseWrapper {
            private boolean injected = false;

            TimingWrapper(HttpServletResponse r) { super(r); }

            void injectHeaders() {
                if (injected) return;
                injected = true;
                setHeader("X-Request-ID", requestId);
                setHeader("X-Response-Time", (System.currentTimeMillis() - startTime) + "ms");
                setHeader("X-Response-Timestamp", Instant.now().toString());
                if (correlationId != null) setHeader("X-Correlation-ID", correlationId);
                if (tenantId != null) setHeader("X-Tenant-ID", tenantId);
            }

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                injectHeaders();
                return super.getOutputStream();
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                injectHeaders();
                return super.getWriter();
            }

            @Override
            public void flushBuffer() throws IOException {
                injectHeaders();
                super.flushBuffer();
            }

            @Override
            public void sendError(int sc) throws IOException {
                injectHeaders();
                super.sendError(sc);
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {
                injectHeaders();
                super.sendError(sc, msg);
            }
        }

        var wrapped = new TimingWrapper(response);
        try {
            chain.doFilter(request, wrapped);
        } finally {
            wrapped.injectHeaders(); // 204 / empty responses that never call getOutputStream
            MDC.clear();
        }
    }
}
