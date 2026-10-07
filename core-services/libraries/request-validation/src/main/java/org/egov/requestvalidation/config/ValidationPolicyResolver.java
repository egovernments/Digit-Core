package org.egov.requestvalidation.config;

import org.egov.requestvalidation.Activation;
import org.egov.requestvalidation.Structured;
import org.egov.requestvalidation.ValidateRequest;
import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.core.ContentExemption;
import org.egov.requestvalidation.core.InspectionLimits;
import org.egov.requestvalidation.core.SkipPathMatcher;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** A body annotation refines an activated handler; it does not opt in a whole controller. */
public final class ValidationPolicyResolver {
    private final RequestValidationProperties properties;
    private final Function<Class<? extends ContentExemption>, ContentExemption> exemptionFactory;
    private final Map<Key, EffectiveValidationPolicy> cache = new ConcurrentHashMap<>();
    private final Map<Class<? extends ContentExemption>, ContentExemption> exemptions = new ConcurrentHashMap<>();

    /** Exemptions are created through their no-argument constructor. */
    public ValidationPolicyResolver(RequestValidationProperties properties) {
        this(properties, BeanUtils::instantiateClass);
    }

    /** exemptionFactory supplies the one instance used for each declared exemption type. */
    public ValidationPolicyResolver(RequestValidationProperties properties,
            Function<Class<? extends ContentExemption>, ContentExemption> exemptionFactory) {
        properties.validate();
        this.properties = properties;
        this.exemptionFactory = Objects.requireNonNull(exemptionFactory, "exemptionFactory");
    }

    public EffectiveValidationPolicy handler(HandlerMethod handler) {
        return resolve(handler.getMethod(), handler.getBeanType(), null);
    }

    public EffectiveValidationPolicy parameter(MethodParameter parameter) {
        if (parameter.getMethod() == null) {
            throw new IllegalArgumentException("Request validation requires a handler method");
        }
        return resolve(parameter.getMethod(), parameter.getContainingClass(), parameter);
    }

    private EffectiveValidationPolicy resolve(Method method, Class<?> type, MethodParameter parameter) {
        Key key = new Key(method, type, parameter == null ? -1 : parameter.getParameterIndex());
        return cache.computeIfAbsent(key, ignored -> build(method, type, parameter));
    }

    private EffectiveValidationPolicy build(Method method, Class<?> type, MethodParameter parameter) {
        ValidateRequest onClass = AnnotatedElementUtils.findMergedAnnotation(type, ValidateRequest.class);
        ValidateRequest onMethod = AnnotatedElementUtils.findMergedAnnotation(method, ValidateRequest.class);
        boolean enabled = properties.getActivation() == Activation.ALL || onClass != null || onMethod != null;
        if (onClass != null) enabled = onClass.enabled();
        if (onMethod != null) enabled = onMethod.enabled();

        boolean structured = properties.getStructuredDefault();
        ValidationMode mode = properties.getMode();
        InspectionLimits limits = properties.getLimits().toLimits();
        LinkedHashSet<String> skips = new LinkedHashSet<>();
        Class<? extends ContentExemption> exemption = null;
        List<ValidateRequest> declarations = new ArrayList<>();
        if (onClass != null) declarations.add(onClass);
        if (onMethod != null) declarations.add(onMethod);
        if (parameter != null) {
            ValidateRequest onParameter = parameter.getParameterAnnotation(ValidateRequest.class);
            if (onParameter != null) declarations.add(onParameter);
        }
        for (ValidateRequest declaration : declarations) {
            if (declaration.structured() != Structured.DEFAULT) {
                structured = declaration.structured() == Structured.ENABLED;
            }
            if (declaration.mode() != ValidationMode.DEFAULT) mode = declaration.mode();
            skips.addAll(Arrays.asList(declaration.skipPaths()));
            if (declaration.exemption() != ContentExemption.None.class) exemption = declaration.exemption();
            limits = new InspectionLimits(
                    inherit(declaration.maxBodyBytes(), limits.getMaxBodyBytes()),
                    inherit(declaration.maxDepth(), limits.getMaxDepth()),
                    inherit(declaration.maxStringLength(), limits.getMaxStringLength()),
                    inherit(declaration.maxNameLength(), limits.getMaxNameLength()),
                    inherit(declaration.maxTokens(), limits.getMaxTokens()),
                    inherit(declaration.maxNumberLength(), limits.getMaxNumberLength()),
                    inherit(declaration.maxScalarLength(), limits.getMaxScalarLength()));
        }
        List<String> paths = EffectiveValidationPolicy.immutableCopy(skips);
        return new EffectiveValidationPolicy(enabled, structured, mode, paths, new SkipPathMatcher(paths), limits,
                exemption == null ? null : exemption(exemption));
    }

    private ContentExemption exemption(Class<? extends ContentExemption> type) {
        return exemptions.computeIfAbsent(type, ignored -> {
            ContentExemption instance;
            try {
                instance = exemptionFactory.apply(type);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Cannot create exemption " + type.getName() + ": " + ex.getMessage(), ex);
            }
            if (instance == null) throw new IllegalArgumentException("No exemption " + type.getName());
            return instance;
        });
    }

    private static int inherit(int value, int fallback) { return value == -1 ? fallback : value; }
    private static long inherit(long value, long fallback) { return value == -1 ? fallback : value; }

    private static final class Key {
        private final Method method;
        private final Class<?> type;
        private final int parameterIndex;

        Key(Method method, Class<?> type, int parameterIndex) {
            this.method = method;
            this.type = type;
            this.parameterIndex = parameterIndex;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Key)) return false;
            Key that = (Key) other;
            return parameterIndex == that.parameterIndex && method.equals(that.method) && type.equals(that.type);
        }

        @Override
        public int hashCode() { return Objects.hash(method, type, parameterIndex); }
    }
}
