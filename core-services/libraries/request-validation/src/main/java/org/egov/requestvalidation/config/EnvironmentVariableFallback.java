package org.egov.requestvalidation.config;

import org.egov.requestvalidation.Activation;
import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.web.ServletSupport;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Environment variables for javax.servlet hosts. Spring Boot 1.5 cannot bind {@code EGOV_REQUEST_VALIDATION_*}
 * variables into the hyphenated {@code egov.request-validation} prefix at all, and Boot 2.x binds the nested ones
 * ({@code limits.*}, {@code rules.*}, {@code log.*}) only in the {@code EGOV_REQUESTVALIDATION_*} form. After binding,
 * each setting whose winning source is an environment-variable source is applied from that source, so
 * {@code EGOV_REQUEST_VALIDATION_<KEY>} works on every supported Boot version with the usual precedence. Boot 3 binds
 * every form itself and is left untouched.
 */
public final class EnvironmentVariableFallback implements BeanPostProcessor, EnvironmentAware, Ordered {
    static final String PREFIX = "egov.request-validation.";
    // Boot 2+ attaches a relaxed view of all sources under this name; skipped so the raw sources decide.
    private static final String ATTACHED_SOURCE = "configurationProperties";

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    // After ConfigurationPropertiesBindingPostProcessor (PriorityOrdered), before afterPropertiesSet validates.
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof RequestValidationProperties && environment instanceof ConfigurableEnvironment
                && !ServletSupport.usesJakartaServlet()) {
            apply((RequestValidationProperties) bean, (ConfigurableEnvironment) environment);
        }
        return bean;
    }

    // Spring 4.3 has no default methods on BeanPostProcessor.
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    static void apply(RequestValidationProperties properties, ConfigurableEnvironment environment) {
        Values values = new Values(environment);
        Boolean bool;
        Integer number;
        String text;
        if ((bool = values.bool("enabled")) != null) properties.setEnabled(bool);
        if ((text = values.get("activation")) != null) properties.setActivation(enumValue(Activation.class, "activation", text));
        if ((text = values.get("mode")) != null) properties.setMode(enumValue(ValidationMode.class, "mode", text));
        if ((bool = values.bool("structured-default")) != null) properties.setStructuredDefault(bool);
        if ((bool = values.bool("inspect-content-type-header")) != null) properties.setInspectContentTypeHeader(bool);
        if ((bool = values.bool("reject-duplicate-keys")) != null) properties.setRejectDuplicateKeys(bool);
        if ((bool = values.bool("reject-dual-request-info")) != null) properties.setRejectDualRequestInfo(bool);

        RequestValidationProperties.Limits limits = properties.getLimits();
        if ((number = values.integer("limits.max-body-bytes")) != null) limits.setMaxBodyBytes(number);
        if ((number = values.integer("limits.max-depth")) != null) limits.setMaxDepth(number);
        if ((number = values.integer("limits.max-string-length")) != null) limits.setMaxStringLength(number);
        if ((number = values.integer("limits.max-name-length")) != null) limits.setMaxNameLength(number);
        if ((text = values.get("limits.max-tokens")) != null) limits.setMaxTokens(parseLong("limits.max-tokens", text));
        if ((number = values.integer("limits.max-number-length")) != null) limits.setMaxNumberLength(number);
        if ((number = values.integer("limits.max-scalar-length")) != null) limits.setMaxScalarLength(number);

        RequestValidationProperties.Rules rules = properties.getRules();
        if ((bool = values.bool("rules.markup-start")) != null) rules.setMarkupStart(bool);
        if ((bool = values.bool("rules.url-scheme")) != null) rules.setUrlScheme(bool);
        if ((bool = values.bool("rules.event-handler")) != null) rules.setEventHandler(bool);
        if ((text = values.get("rules.disallowed-controls")) != null) rules.setDisallowedControls(integers("rules.disallowed-controls", text));
        if ((text = values.get("rules.denied-schemes")) != null) rules.setDeniedSchemes(strings(text));
        if ((text = values.get("rules.denied-data-media-types")) != null) rules.setDeniedDataMediaTypes(strings(text));
        if ((number = values.integer("rules.decode-rounds")) != null) rules.setDecodeRounds(number);
        if ((bool = values.bool("rules.normalize-nfkc")) != null) rules.setNormalizeNfkc(bool);

        if ((text = values.get("log.report-sample-rate")) != null) properties.getLog().setReportSampleRate(parseDouble("log.report-sample-rate", text));
    }

    /** The value for a key when an environment-variable source is the one that wins for it; otherwise null. */
    static final class Values {
        private final ConfigurableEnvironment environment;

        Values(ConfigurableEnvironment environment) {
            this.environment = environment;
        }

        String get(String key) {
            String name = PREFIX + key;
            for (PropertySource<?> source : environment.getPropertySources()) {
                if (ATTACHED_SOURCE.equals(source.getName())) continue;
                Object value = source.getProperty(name);
                if (value != null) {
                    if (!(source instanceof SystemEnvironmentPropertySource)) return null;
                    String text = String.valueOf(value).trim();
                    return text.isEmpty() ? null : text;
                }
            }
            return null;
        }

        Boolean bool(String key) {
            String text = get(key);
            if (text == null) return null;
            switch (text.toLowerCase(Locale.ROOT)) {
                case "true": case "on": case "yes": case "1": return Boolean.TRUE;
                case "false": case "off": case "no": case "0": return Boolean.FALSE;
                default: throw invalid(key);
            }
        }

        Integer integer(String key) {
            String text = get(key);
            if (text == null) return null;
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ex) {
                throw invalid(key);
            }
        }
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String key, String text) {
        try {
            return Enum.valueOf(type, text.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException ex) {
            throw invalid(key);
        }
    }

    private static long parseLong(String key, String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            throw invalid(key);
        }
    }

    private static double parseDouble(String key, String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw invalid(key);
        }
    }

    private static List<Integer> integers(String key, String text) {
        List<Integer> values = new ArrayList<>();
        for (String item : StringUtils.commaDelimitedListToStringArray(text)) {
            try {
                values.add(Integer.valueOf(item.trim()));
            } catch (NumberFormatException ex) {
                throw invalid(key);
            }
        }
        return values;
    }

    private static List<String> strings(String text) {
        List<String> values = new ArrayList<>();
        for (String item : StringUtils.commaDelimitedListToStringArray(text)) values.add(item.trim());
        return values;
    }

    // Configuration text is not echoed.
    private static IllegalArgumentException invalid(String key) {
        return new IllegalArgumentException("Invalid value in the environment variable for " + PREFIX + key);
    }
}
