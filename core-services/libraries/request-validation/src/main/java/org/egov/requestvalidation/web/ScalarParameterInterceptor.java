package org.egov.requestvalidation.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.egov.requestvalidation.config.EffectiveValidationPolicy;
import org.egov.requestvalidation.config.RequestValidationProperties;
import org.egov.requestvalidation.config.ValidationPolicyResolver;
import org.egov.requestvalidation.core.ContentDetector;
import org.egov.requestvalidation.core.SafeLocationFormatter;
import org.egov.requestvalidation.core.Violation;
import org.egov.requestvalidation.core.ViolationCode;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

public final class ScalarParameterInterceptor implements HandlerInterceptor {
    private final ValidationPolicyResolver resolver;
    private final ContentDetector detector;
    private final ValidationReporter reporter;
    private final boolean inspectContentType;

    public ScalarParameterInterceptor(ValidationPolicyResolver resolver, ContentDetector detector,
                                      ValidationReporter reporter, RequestValidationProperties properties) {
        this.resolver = resolver;
        this.detector = detector;
        this.reporter = reporter;
        inspectContentType = properties.isInspectContentTypeHeader();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) return true;
        EffectiveValidationPolicy policy = resolver.handler(method);
        if (!policy.enabled()) return true;
        String handlerName = method.getBeanType().getName() + "#" + method.getMethod().getName();
        // Counters are local, never shared across servlet threads.
        long[] budget = {0, 0};
        if (inspectContentType && request.getContentType() != null
                && !inspect(request, request.getContentType(), "/Content-Type", "header", handlerName, policy, budget)) {
            return true;
        }
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String location = SafeLocationFormatter.scalar(entry.getKey());
            if (!inspect(request, entry.getKey(), location, "query/form", handlerName, policy, budget)) return true;
            if (entry.getValue() != null) {
                for (String value : entry.getValue()) {
                    if (!inspect(request, value, location, "query/form", handlerName, policy, budget)) return true;
                }
            }
        }
        Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables instanceof Map<?, ?> paths) {
            for (Map.Entry<?, ?> entry : paths.entrySet()) {
                if (entry.getKey() instanceof String name && entry.getValue() instanceof String value) {
                    String location = SafeLocationFormatter.scalar(name);
                    if (!inspect(request, name, location, "path", handlerName, policy, budget)
                            || !inspect(request, value, location, "path", handlerName, policy, budget)) {
                        return true;
                    }
                }
            }
        }
        return true;
    }

    /** Returns false when REPORT stops scalar inspection at a limit; the host then converts as usual. */
    private boolean inspect(HttpServletRequest request, String value, String location, String kind,
                            String handler, EffectiveValidationPolicy policy, long[] budget) {
        if (value == null) return true;
        budget[0]++;
        budget[1] += value.length();
        if (value.length() > policy.limits().getMaxScalarLength()
                || budget[0] > policy.limits().getMaxTokens()
                || budget[1] > policy.limits().getMaxBodyBytes()) {
            ContentPolicyViolationException rejection = reporter.structural(request, policy.mode(),
                    new Violation(ViolationCode.REQUEST_LIMIT_EXCEEDED, "scalar-limit", location, value.length()),
                    kind, handler);
            if (rejection != null) throw rejection;
            return false;
        }
        detector.detect(value).ifPresent(rule -> reporter.content(request, policy.mode(),
                new Violation(ViolationCode.REQUEST_CONTENT_NOT_ALLOWED, rule, location, value.length()), kind, handler));
        return true;
    }
}
