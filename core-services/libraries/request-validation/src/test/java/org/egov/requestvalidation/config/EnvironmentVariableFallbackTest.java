package org.egov.requestvalidation.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentVariableFallbackTest {

    /** The settings found by reflection are exactly those in the configuration metadata the annotation processor generated. */
    @Test
    void knowsEverySetting() throws Exception {
        List<String> generated = new ArrayList<>();
        try (InputStream metadata = getClass().getResourceAsStream("/META-INF/spring-configuration-metadata.json");
             JsonParser parser = new JsonFactory().createParser(metadata)) {
            assertThat(metadata).isNotNull();
            parser.nextToken();
            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                String section = parser.getCurrentName();
                parser.nextToken();
                if (!"properties".equals(section)) {
                    parser.skipChildren();
                    continue;
                }
                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() == JsonToken.FIELD_NAME) {
                        String field = parser.getCurrentName();
                        parser.nextToken();
                        if ("name".equals(field)) generated.add(parser.getText());
                        else parser.skipChildren();
                    }
                }
            }
        }
        assertThat(generated).hasSize(23);
        assertThat(new TreeSet<>(EnvironmentVariableFallback.KEYS)).isEqualTo(new TreeSet<>(generated));
    }

    @Test
    void declaresAVariableNoOtherSourceMentionsDirectlyAfterTheEnvironment() {
        MutablePropertySources sources = sources(variables("EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH", "${DEPTH}",
                "EGOV_REQUEST_VALIDATION_MODE", "ENFORCE", "DEPTH", "9"));
        sources.addLast(new MapPropertySource("applicationProperties",
                Collections.<String, Object>singletonMap("egov.request-validation.enabled", "true")));
        EnvironmentVariableFallback.declare(sources);

        PropertySource<?> declared = sources.get(EnvironmentVariableFallback.SOURCE_NAME);
        assertThat(declared).isNotNull();
        assertThat(sources.precedenceOf(declared))
                .isEqualTo(sources.precedenceOf(sources.get(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) + 1);
        // Raw values: Spring resolves placeholders and converts when it binds.
        assertThat(((EnumerablePropertySource<?>) declared).getPropertyNames())
                .containsExactlyInAnyOrder("egov.request-validation.mode", "egov.request-validation.limits.max-depth");
        assertThat(declared.getProperty("egov.request-validation.limits.max-depth")).isEqualTo("${DEPTH}");
        assertThat(declared.getProperty("egov.request-validation.mode")).isEqualTo("ENFORCE");
    }

    @Test
    void leavesASettingThatAnotherSourceSpellsInAnyFormToSpring() {
        String[] spellings = {"egov.request-validation.mode", "egov.requestValidation.mode", "egov.request_validation.mode",
                "EGOV_REQUEST_VALIDATION_MODE", "egov.request-validation.rules.denied-schemes[0]"};
        for (String spelling : spellings) {
            String key = spelling.contains("denied") ? "EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES" : "EGOV_REQUEST_VALIDATION_MODE";
            MutablePropertySources sources = sources(variables(key, "x"));
            sources.addFirst(new MapPropertySource("commandLineArgs", Collections.<String, Object>singletonMap(spelling, "y")));
            EnvironmentVariableFallback.declare(sources);
            assertThat(sources.get(EnvironmentVariableFallback.SOURCE_NAME)).as(spelling).isNull();
        }
    }

    @Test
    void readsNamesInsideCompositeSourcesAndSkipsSourcesThatCannotListThem() {
        MutablePropertySources sources = sources(variables("EGOV_REQUEST_VALIDATION_MODE", "ENFORCE",
                "EGOV_REQUEST_VALIDATION_ENABLED", "true"));
        CompositePropertySource composite = new CompositePropertySource("composite");
        composite.addPropertySource(new MapPropertySource("nested",
                Collections.<String, Object>singletonMap("egov.request-validation.mode", "REPORT")));
        sources.addLast(composite);
        sources.addLast(new EnumerablePropertySource<Object>("broken", new Object()) {
            @Override public String[] getPropertyNames() { throw new IllegalStateException("cannot list"); }
            @Override public Object getProperty(String name) { return null; }
        });
        EnvironmentVariableFallback.declare(sources);
        assertThat(((EnumerablePropertySource<?>) sources.get(EnvironmentVariableFallback.SOURCE_NAME)).getPropertyNames())
                .containsExactly("egov.request-validation.enabled");
    }

    @Test
    void addsNothingWithoutVariablesAndReplacesAnEarlierDeclaration() {
        MutablePropertySources sources = sources(variables("PATH", "/usr/bin"));
        EnvironmentVariableFallback.declare(sources);
        assertThat(sources.get(EnvironmentVariableFallback.SOURCE_NAME)).isNull();

        sources = sources(variables("EGOV_REQUEST_VALIDATION_MODE", "ENFORCE"));
        EnvironmentVariableFallback.declare(sources);
        EnvironmentVariableFallback.declare(sources);
        int count = 0;
        for (PropertySource<?> source : sources) if (source.getName().equals(EnvironmentVariableFallback.SOURCE_NAME)) count++;
        assertThat(count).isEqualTo(1);

        MutablePropertySources noEnvironment = new MutablePropertySources();
        EnvironmentVariableFallback.declare(noEnvironment);
        assertThat(noEnvironment.size()).isZero();
    }

    @Test
    void declaresAnEmptyListButNoOtherBlankSetting() {
        MutablePropertySources sources = sources(variables("EGOV_REQUEST_VALIDATION_ACTIVATION", "",
                "EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH", "  ", "EGOV_REQUEST_VALIDATION_RULES_URL_SCHEME", "${RV_EMPTY:}",
                "EGOV_REQUEST_VALIDATION_LOG_REPORT_SAMPLE_RATE", "${RV_BLANK}", "RV_BLANK", " ",
                "EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES", "${RV_LIST:}",
                "EGOV_REQUEST_VALIDATION_RULES_DENIED_DATA_MEDIA_TYPES", " ",
                "EGOV_REQUEST_VALIDATION_MODE", "${RV_MODE}", "RV_MODE", "ENFORCE",
                "EGOV_REQUEST_VALIDATION_RULES_DECODE_ROUNDS", "${RV_MISSING}"));
        EnvironmentVariableFallback.declare(sources);
        PropertySource<?> declared = sources.get(EnvironmentVariableFallback.SOURCE_NAME);
        // Unresolvable placeholders are declared as they are: Spring reports them when it binds, as without the library.
        assertThat(((EnumerablePropertySource<?>) declared).getPropertyNames()).containsExactlyInAnyOrder(
                "egov.request-validation.mode", "egov.request-validation.rules.denied-schemes",
                "egov.request-validation.rules.decode-rounds");
        assertThat(declared.getProperty("egov.request-validation.mode")).isEqualTo("${RV_MODE}");
    }

    @Test
    void readsEveryEnvironmentSourceAndDeclaresBelowTheLast() {
        MutablePropertySources sources = sources(variables("EGOV_REQUEST_VALIDATION_MODE", "ENFORCE"));
        sources.addFirst(new SystemEnvironmentPropertySource("test", variables("OTHER", "x")));
        EnvironmentVariableFallback.declare(sources);
        PropertySource<?> declared = sources.get(EnvironmentVariableFallback.SOURCE_NAME);
        assertThat(declared.getProperty("egov.request-validation.mode")).isEqualTo("ENFORCE");
        assertThat(sources.precedenceOf(declared))
                .isEqualTo(sources.precedenceOf(sources.get(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) + 1);
    }

    @Test
    void normalizeIgnoresCaseSeparatorsAndIndexes() {
        assertThat(EnvironmentVariableFallback.normalize("egov.requestValidation.rules.denied_schemes[3]"))
                .isEqualTo(EnvironmentVariableFallback.normalize("egov.request-validation.rules.denied-schemes"));
        assertThat(EnvironmentVariableFallback.normalize("egov.request-validation.mode"))
                .isNotEqualTo(EnvironmentVariableFallback.normalize("egov.request-validation.activation"));
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

    private static Map<String, Object> variables(String... keysAndValues) {
        Map<String, Object> variables = new HashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) variables.put(keysAndValues[i], keysAndValues[i + 1]);
        return variables;
    }

    private static MutablePropertySources sources(Map<String, Object> variables) {
        MutablePropertySources sources = new StandardEnvironment().getPropertySources();
        sources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, variables));
        sources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return sources;
    }
}
