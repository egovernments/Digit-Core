package org.egov.requestvalidation.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentVariableFallbackTest {

    /** Every bindable setting must be applied from its EGOV_REQUEST_VALIDATION_* variable; catches a setting added later. */
    @Test
    void appliesEverySettingFromItsEnvironmentVariable() throws Exception {
        RequestValidationProperties defaults = new RequestValidationProperties();
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> expected = new LinkedHashMap<>();
        collect("", RequestValidationProperties.class, defaults, variables, expected);
        collect("limits.", RequestValidationProperties.Limits.class, defaults.getLimits(), variables, expected);
        collect("rules.", RequestValidationProperties.Rules.class, defaults.getRules(), variables, expected);
        collect("log.", RequestValidationProperties.Log.class, defaults.getLog(), variables, expected);
        assertThat(expected).hasSize(23);

        RequestValidationProperties properties = new RequestValidationProperties();
        EnvironmentVariableFallback.apply(properties, environment(variables));

        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            assertThat(read(properties, entry.getKey())).as(entry.getKey()).isEqualTo(entry.getValue());
        }
    }

    @Test
    void aHigherPrecedenceSourceStillWins() {
        StandardEnvironment environment = environment(Collections.<String, Object>singletonMap(
                "EGOV_REQUEST_VALIDATION_MODE", "REPORT"));
        environment.getPropertySources().addFirst(new MapPropertySource("commandLine",
                Collections.<String, Object>singletonMap("egov.request-validation.mode", "ENFORCE")));
        RequestValidationProperties properties = new RequestValidationProperties();
        properties.setMode(org.egov.requestvalidation.ValidationMode.ENFORCE);
        EnvironmentVariableFallback.apply(properties, environment);
        assertThat(properties.getMode()).isEqualTo(org.egov.requestvalidation.ValidationMode.ENFORCE);
    }

    @Test
    void anEnvironmentVariableOverridesALowerPrecedenceFile() {
        StandardEnvironment environment = environment(Collections.<String, Object>singletonMap(
                "EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH", "9"));
        environment.getPropertySources().addLast(new MapPropertySource("applicationProperties",
                Collections.<String, Object>singletonMap("egov.request-validation.limits.max-depth", "5")));
        RequestValidationProperties properties = new RequestValidationProperties();
        EnvironmentVariableFallback.apply(properties, environment);
        assertThat(properties.getLimits().getMaxDepth()).isEqualTo(9);
    }

    @Test
    void ignoresBootsAttachedViewAndAppliesNothingWithoutVariables() {
        StandardEnvironment environment = environment(Collections.<String, Object>emptyMap());
        environment.getPropertySources().addFirst(new MapPropertySource("configurationProperties",
                Collections.<String, Object>singletonMap("egov.request-validation.mode", "ENFORCE")));
        RequestValidationProperties properties = new RequestValidationProperties();
        EnvironmentVariableFallback.apply(properties, environment);
        assertThat(properties.getMode()).isEqualTo(org.egov.requestvalidation.ValidationMode.REPORT);
        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getRules().getDeniedSchemes()).isNull();
    }

    @Test
    void lenientEnumsAndListsAndEmptyValues() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("EGOV_REQUEST_VALIDATION_MODE", " enforce ");
        variables.put("EGOV_REQUEST_VALIDATION_ACTIVATION", "all");
        variables.put("EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES", "javascript, vbscript ,livescript");
        variables.put("EGOV_REQUEST_VALIDATION_RULES_DISALLOWED_CONTROLS", "0, 11");
        variables.put("EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT", "");
        RequestValidationProperties properties = new RequestValidationProperties();
        EnvironmentVariableFallback.apply(properties, environment(variables));
        assertThat(properties.getMode()).isEqualTo(org.egov.requestvalidation.ValidationMode.ENFORCE);
        assertThat(properties.getActivation()).isEqualTo(org.egov.requestvalidation.Activation.ALL);
        assertThat(properties.getRules().getDeniedSchemes()).containsExactly("javascript", "vbscript", "livescript");
        assertThat(properties.getRules().getDisallowedControls()).containsExactly(0, 11);
        assertThat(properties.getStructuredDefault()).isNull();
    }

    @Test
    void invalidValuesFailNamingTheSettingButNotTheValue() {
        for (String[] variable : new String[][] {{"EGOV_REQUEST_VALIDATION_ENABLED", "maybe"},
                {"EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH", "deep"}, {"EGOV_REQUEST_VALIDATION_MODE", "strict"},
                {"EGOV_REQUEST_VALIDATION_RULES_DISALLOWED_CONTROLS", "0,x"}}) {
            StandardEnvironment environment = environment(Collections.<String, Object>singletonMap(variable[0], variable[1]));
            assertThatThrownBy(() -> EnvironmentVariableFallback.apply(new RequestValidationProperties(), environment))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("egov.request-validation.")
                    .hasMessageNotContaining(variable[1]);
        }
    }

    @Test
    void defaultsAreAppliedWhenTheListsAreNotConfigured() {
        RequestValidationProperties.Rules rules = new RequestValidationProperties().getRules();
        assertThat(rules.toPolicy().getDeniedSchemes()).containsExactly("javascript", "vbscript");
        assertThat(rules.toPolicy().getDisallowedControls()).containsExactly(0);
        assertThat(rules.toPolicy().getDeniedDataMediaTypes())
                .containsExactly("text/html", "application/xhtml+xml", "image/svg+xml");
        rules.setDeniedSchemes(Collections.singletonList("livescript"));
        assertThat(rules.toPolicy().getDeniedSchemes()).containsExactly("livescript");
    }

    private static StandardEnvironment environment(Map<String, Object> variables) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, variables));
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return environment;
    }

    /** One variable per setting, with a value different from the default; records the value expected after apply(). */
    private static void collect(String prefix, Class<?> type, Object defaults, Map<String, Object> variables,
                                Map<String, Object> expected) throws Exception {
        for (PropertyDescriptor property : Introspector.getBeanInfo(type, Object.class).getPropertyDescriptors()) {
            if (property.getWriteMethod() == null) continue;
            String key = prefix + kebab(property.getName());
            Class<?> valueType = property.getPropertyType();
            Object current = property.getReadMethod().invoke(defaults);
            String text;
            Object value;
            if (valueType == boolean.class || valueType == Boolean.class) {
                value = current == null ? Boolean.TRUE : !(Boolean) current;
                text = String.valueOf(value);
            } else if (valueType == int.class) {
                value = (Integer) current + 3;
                text = String.valueOf(value);
            } else if (valueType == long.class) {
                value = (Long) current + 3;
                text = String.valueOf(value);
            } else if (valueType == double.class) {
                value = 0.25d;
                text = "0.25";
            } else if (valueType.isEnum()) {
                value = otherConstant(valueType, current);
                text = ((Enum<?>) value).name();
            } else if (valueType == List.class) {
                Class<?> element = (Class<?>) ((ParameterizedType) property.getWriteMethod().getGenericParameterTypes()[0])
                        .getActualTypeArguments()[0];
                value = element == Integer.class ? Arrays.asList(0, 11) : Arrays.asList("x-one", "x-two");
                text = element == Integer.class ? "0,11" : "x-one,x-two";
            } else {
                throw new AssertionError("No test value for " + key + " of type " + valueType);
            }
            variables.put(("EGOV_REQUEST_VALIDATION_" + key).toUpperCase().replace('.', '_').replace('-', '_'), text);
            expected.put(key, value);
        }
    }

    private static Object otherConstant(Class<?> type, Object current) {
        for (Object constant : type.getEnumConstants()) {
            if (!constant.equals(current) && !"DEFAULT".equals(((Enum<?>) constant).name())) return constant;
        }
        throw new AssertionError("No other constant in " + type);
    }

    private static Object read(RequestValidationProperties properties, String key) throws Exception {
        Object target = properties;
        String name = key;
        int dot = key.indexOf('.');
        if (dot > 0) {
            String group = key.substring(0, dot);
            target = group.equals("limits") ? properties.getLimits() : group.equals("rules") ? properties.getRules() : properties.getLog();
            name = key.substring(dot + 1);
        }
        for (PropertyDescriptor property : Introspector.getBeanInfo(target.getClass(), Object.class).getPropertyDescriptors()) {
            if (kebab(property.getName()).equals(name)) return property.getReadMethod().invoke(target);
        }
        throw new AssertionError("No property " + key);
    }

    private static String kebab(String camel) {
        return camel.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }
}
