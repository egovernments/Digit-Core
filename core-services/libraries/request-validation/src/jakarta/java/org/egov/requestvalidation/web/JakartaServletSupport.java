package org.egov.requestvalidation.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/** {@link ServletSupport} for jakarta.servlet (Spring Boot 3.x; compiled separately against Spring 6). Loaded only by {@link ServletSupport#forRuntime()}. */
final class JakartaServletSupport implements ServletSupport {
    @Override
    public HandlerInterceptor scalarInterceptor(ScalarParameterInterceptor checks) {
        return new ScalarInterceptor(checks);
    }

    @Override
    public ValidationRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return new Request(((ServletRequestAttributes) attributes).getRequest());
        }
        throw new IllegalStateException("Request validation requires a servlet request context");
    }

    static final class ScalarInterceptor implements HandlerInterceptor {
        private final ScalarParameterInterceptor checks;

        ScalarInterceptor(ScalarParameterInterceptor checks) { this.checks = checks; }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            return checks.preHandle(new Request(request), handler);
        }
    }

    static final class Request implements ValidationRequest {
        private final HttpServletRequest request;

        Request(HttpServletRequest request) { this.request = request; }

        @Override public String getMethod() { return request.getMethod(); }
        @Override public String getContentType() { return request.getContentType(); }
        @Override public Map<String, String[]> getParameterMap() { return request.getParameterMap(); }
        @Override public Object getAttribute(String name) { return request.getAttribute(name); }
        @Override public void setAttribute(String name, Object value) { request.setAttribute(name, value); }
    }
}
