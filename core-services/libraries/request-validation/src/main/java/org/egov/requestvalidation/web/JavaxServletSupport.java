package org.egov.requestvalidation.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/** {@link ServletSupport} for javax.servlet (Spring Boot 1.5 and 2.x). Loaded only by {@link ServletSupport#forRuntime()}. */
final class JavaxServletSupport implements ServletSupport {
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

        // Explicit no-ops (the Spring 5 defaults); Spring 4.3 (Boot 1.5) declares these methods abstract.
        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                               ModelAndView modelAndView) {
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                    Exception ex) {
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
