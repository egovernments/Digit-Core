package org.egov.requestvalidation.web;

import org.egov.requestvalidation.Structured;
import org.egov.requestvalidation.ValidateRequest;
import org.egov.requestvalidation.config.EffectiveValidationPolicy;
import org.egov.requestvalidation.config.ValidationPolicyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class CoverageReport implements SmartInitializingSingleton {
    private static final Logger LOG = LoggerFactory.getLogger(CoverageReport.class);
    private final ObjectProvider<RequestMappingHandlerMapping> mappings;
    private final ValidationPolicyResolver resolver;

    public CoverageReport(ObjectProvider<RequestMappingHandlerMapping> mappings, ValidationPolicyResolver resolver) {
        this.mappings = mappings;
        this.resolver = resolver;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Set<HandlerMethod> handlers = new LinkedHashSet<>();
        mappings.orderedStream().forEach(mapping -> handlers.addAll(mapping.getHandlerMethods().values()));
        for (HandlerMethod handler : handlers) {
            String name = handler.getBeanType().getName() + "#" + handler.getMethod().getName();
            EffectiveValidationPolicy policy = resolve(name, () -> resolver.handler(handler));
            LOG.info("request_validation_coverage handler={} enabled={} mode={}", name, policy.enabled(), policy.mode());
            declaration(name, AnnotatedElementUtils.findMergedAnnotation(handler.getBeanType(), ValidateRequest.class), false);
            declaration(name, handler.getMethodAnnotation(ValidateRequest.class), false);
            for (MethodParameter parameter : handler.getMethodParameters()) {
                ValidateRequest annotation = parameter.getParameterAnnotation(ValidateRequest.class);
                declaration(name, annotation, true);
                if (annotation != null && !policy.enabled()) {
                    LOG.warn("request_validation_parameter_in_inactive_handler handler={} index={}", name, parameter.getParameterIndex());
                }
                // Resolve every body at startup, including disabled handlers, to catch invalid limits/paths.
                if (StructuredBodyAdvice.isBody(parameter)) {
                    EffectiveValidationPolicy body = resolve(name, () -> resolver.parameter(parameter));
                    LOG.info("request_validation_body handler={} index={} enabled={} structured={} mode={} skipPaths={}",
                            name, parameter.getParameterIndex(), body.enabled(), body.structured(), body.mode(),
                            display(body.skipPaths()));
                } else if (annotation != null) {
                    LOG.warn("request_validation_scalar_settings_ignored handler={} index={}", name, parameter.getParameterIndex());
                }
            }
        }
    }

    /** Names the handler whose declaration is invalid, so a bad limit or skip path fails startup legibly. */
    private static EffectiveValidationPolicy resolve(String handler, Supplier<EffectiveValidationPolicy> policy) {
        try {
            return policy.get();
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid @ValidateRequest on " + handler + ": " + ex.getMessage(), ex);
        }
    }

    /** Quoted, escaped elements keep [] vs ["/"] and ["/a, /b"] vs ["/a", "/b"] distinguishable. */
    private static String display(List<String> skipPaths) {
        StringBuilder text = new StringBuilder("[");
        for (int index = 0; index < skipPaths.size(); index++) {
            if (index > 0) text.append(", ");
            String safe = ValidationAuditLogger.safeText(skipPaths.get(index));
            text.append('"').append(safe.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        return text.append(']').toString();
    }

    private static void declaration(String handler, ValidateRequest annotation, boolean parameter) {
        if (annotation == null) return;
        if (parameter && !annotation.enabled()) {
            LOG.warn("request_validation_parameter_enabled_ignored handler={}", handler);
        }
        boolean exclusion = (!parameter && !annotation.enabled())
                || annotation.structured() == Structured.DISABLED || annotation.skipPaths().length > 0;
        if (exclusion) {
            if (annotation.reason().isBlank()) {
                LOG.warn("request_validation_exclusion_missing_reason handler={}", handler);
            } else {
                LOG.info("request_validation_exclusion handler={} reason={}", handler,
                        ValidationAuditLogger.safeText(annotation.reason()));
            }
        }
    }
}
