package org.egov.requestvalidation.config;

import org.egov.requestvalidation.web.ServletSupport;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Environment variables for javax.servlet hosts. Spring Boot 1.5 cannot map an {@code EGOV_REQUEST_VALIDATION_*}
 * variable back to the hyphenated {@code egov.request-validation} prefix, and Boot 2.0-2.6 skip a nested group
 * ({@code limits}, {@code rules}, {@code log}) that no other source mentions. Before the application beans are
 * created, each setting that is given by such a variable and mentioned by no other source is declared under its
 * property name, directly after the environment-variable sources. Spring then binds it as it binds a key in
 * application.properties that a variable overrides: Spring resolves placeholders, converts the value and applies
 * precedence. Boot 3 binds every form itself and is left untouched.
 */
public final class EnvironmentVariableFallback implements BeanFactoryPostProcessor, EnvironmentAware {
    static final String SOURCE_NAME = "requestValidationEnvironmentVariables";
    static final String PREFIX = "egov.request-validation";
    // Boot 2+ attaches a relaxed view of all sources under this name; skipped so the raw sources decide.
    private static final String ATTACHED_SOURCE = "configurationProperties";
    private static final Map<String, Class<?>> SETTINGS = settings(PREFIX, RequestValidationProperties.class);
    static final List<String> KEYS = Collections.unmodifiableList(new ArrayList<>(SETTINGS.keySet()));

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        if (environment instanceof ConfigurableEnvironment && !ServletSupport.usesJakartaServlet()) {
            declare(((ConfigurableEnvironment) environment).getPropertySources());
        }
    }

    static void declare(MutablePropertySources sources) {
        sources.remove(SOURCE_NAME);
        List<SystemEnvironmentPropertySource> variables = new ArrayList<>();
        Set<String> mentioned = new HashSet<>();
        for (PropertySource<?> source : sources) {
            if (source instanceof SystemEnvironmentPropertySource) {
                variables.add((SystemEnvironmentPropertySource) source);
            } else if (!ATTACHED_SOURCE.equals(source.getName())) {
                collectNames(source, mentioned);
            }
        }
        if (variables.isEmpty()) return;
        PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(sources);
        Map<String, Object> declared = new LinkedHashMap<>();
        for (Map.Entry<String, Class<?>> setting : SETTINGS.entrySet()) {
            String key = setting.getKey();
            // Spelled in any form by another source: Spring binds it from there and resolves the variable itself.
            if (mentioned.contains(normalize(key))) continue;
            Object value = value(variables, key);
            if (value == null) continue;
            // A value that resolves to blank binds to nothing, and Boot would report it as unbound here (environment
            // variables are exempt): it keeps its default, as without this library. Only an empty list is declared.
            String resolved = resolver.resolvePlaceholders(String.valueOf(value));
            if (resolved.trim().isEmpty() && !(resolved.isEmpty() && isList(setting.getValue()))) continue;
            declared.put(key, value);
        }
        // Below every environment-variable source, so the variables themselves still decide the value.
        String last = variables.get(variables.size() - 1).getName();
        if (!declared.isEmpty()) sources.addAfter(last, new MapPropertySource(SOURCE_NAME, declared));
    }

    // Spring's own lookup: EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH and the other spellings it accepts.
    private static Object value(List<SystemEnvironmentPropertySource> variables, String key) {
        for (SystemEnvironmentPropertySource source : variables) {
            Object value = source.getProperty(key);
            if (value != null) return value;
        }
        return null;
    }

    private static boolean isList(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || type.isArray();
    }

    // A source that cannot list its names is looked up by name only, like a non-enumerable one.
    private static void collectNames(PropertySource<?> source, Set<String> names) {
        if (source instanceof CompositePropertySource) {
            for (PropertySource<?> nested : ((CompositePropertySource) source).getPropertySources()) {
                if (!(nested instanceof SystemEnvironmentPropertySource)) collectNames(nested, names);
            }
        } else if (source instanceof EnumerablePropertySource) {
            String[] sourceNames;
            try {
                sourceNames = ((EnumerablePropertySource<?>) source).getPropertyNames();
            } catch (RuntimeException ex) {
                return;
            }
            for (String name : sourceNames) names.add(normalize(name));
        }
    }

    /** Ignores case, separators and a list index, so camelCase, underscore and indexed spellings match. */
    static String normalize(String name) {
        int index = name.indexOf('[');
        String base = index < 0 ? name : name.substring(0, index);
        StringBuilder normalized = new StringBuilder(base.length());
        for (char c : base.toCharArray()) {
            if (c != '.' && c != '-' && c != '_') normalized.append(Character.toLowerCase(c));
        }
        return normalized.toString();
    }

    /** Every bindable setting and its type: writable properties, recursing into the nested groups. */
    static Map<String, Class<?>> settings(String prefix, Class<?> type) {
        Map<String, Class<?>> settings = new LinkedHashMap<>();
        for (PropertyDescriptor property : BeanUtils.getPropertyDescriptors(type)) {
            if (property.getReadMethod() == null || property.getPropertyType() == null) continue;
            String key = prefix + "." + kebab(property.getName());
            if (property.getWriteMethod() != null) {
                settings.put(key, property.getPropertyType());
            } else if (property.getPropertyType().getEnclosingClass() == RequestValidationProperties.class) {
                settings.putAll(settings(key, property.getPropertyType()));
            }
        }
        return settings;
    }

    private static String kebab(String camel) {
        return camel.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }
}
