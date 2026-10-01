package io.digit.requestvalidation;

import io.digit.requestvalidation.autoconfigure.RequestValidationAutoConfiguration;
import org.egov.requestvalidation.ValidateRequest;
import org.egov.requestvalidation.config.RequestValidationProperties;
import org.egov.requestvalidation.web.ContentPolicyViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Settings given as OS environment variables, in the same scenarios and with the same expected results on every
 * supported Spring Boot line (Boot 3 binds every form itself). The same file is in src/boot1 and src/boot2plus.
 */
class EnvironmentVariablesTest {
    private static final String ENV = StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
    private static final String SYS = StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;
    private static final Map<String, Object> NONE = Collections.emptyMap();
    private static final String[] FILE = {"egov.request-validation.enabled=true",
            "egov.request-validation.structured-default=true", "egov.request-validation.mode=REPORT"};
    private static final String DEFAULT_RULES = "schemes=[javascript, vbscript] media=[text/html, application/xhtml+xml, image/svg+xml]";

    @Test
    void environmentVariablesAlone() {
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_ENABLED=true", "EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT=true",
                "EGOV_REQUEST_VALIDATION_MODE=ENFORCE", "EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=3",
                "EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES=livescript,vbscript"), NONE))
                .isEqualTo("mode=ENFORCE depth=3 schemes=[livescript, vbscript] media=[text/html, application/xhtml+xml, image/svg+xml]");
    }

    @Test
    void environmentVariablesOverrideTheFile() {
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_MODE=ENFORCE", "EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=9",
                "EGOV_REQUEST_VALIDATION_LOG_REPORT_SAMPLE_RATE=0.5"), NONE, plus(FILE, "egov.request-validation.limits.max-depth=5")))
                .isEqualTo("mode=ENFORCE depth=9 " + DEFAULT_RULES);
        assertThat(sampleRate(map("EGOV_REQUEST_VALIDATION_LOG_REPORT_SAMPLE_RATE=0.5"))).isEqualTo(0.5);
    }

    @Test
    void placeholdersInVariablesAreResolved() {
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_MODE=${RV_MODE}", "RV_MODE=ENFORCE",
                "EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=${RV_DEPTH}", "RV_DEPTH=7",
                "EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES=${RV_SCHEMES}", "RV_SCHEMES=livescript"), NONE))
                .isEqualTo("mode=ENFORCE depth=7 schemes=[livescript] media=[text/html, application/xhtml+xml, image/svg+xml]");
    }

    @Test
    void valuesAreConvertedAsSpringConvertsThem() {
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=0x10", "EGOV_REQUEST_VALIDATION_MODE=enforce"), NONE))
                .isEqualTo("mode=ENFORCE depth=16 " + DEFAULT_RULES);
    }

    /** A setting another source mentions is left to Spring (Boot 1.5 has its own rules for a relaxed prefix). */
    @Test
    void aHigherPrecedenceSourceWins() {
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_MODE=REPORT", "EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=9"),
                map("egov.request-validation.mode=ENFORCE", "egov.request-validation.limits.max-depth=7")))
                .isEqualTo("mode=ENFORCE depth=7 " + DEFAULT_RULES);
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=9"), map("egov.request-validation.limits.maxDepth=7")))
                .isEqualTo("mode=REPORT depth=7 " + DEFAULT_RULES);
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=9"), map("egov.request-validation.limits.max_depth=7")))
                .isEqualTo("mode=REPORT depth=7 " + DEFAULT_RULES);
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES=envscheme"),
                map("egov.request-validation.rules.denied-schemes[0]=sysscheme")))
                .isEqualTo("mode=REPORT depth=64 schemes=[sysscheme] media=[text/html, application/xhtml+xml, image/svg+xml]");
    }

    @Test
    void listsReplaceTheDefaults() {
        assertThat(bound(NONE, NONE, plus(FILE, "egov.request-validation.rules.denied-schemes[0]=livescript")))
                .isEqualTo("mode=REPORT depth=64 schemes=[livescript] media=[text/html, application/xhtml+xml, image/svg+xml]");
        assertThat(bound(NONE, NONE, plus(FILE, "egov.request-validation.rules.denied-schemes=livescript,javascript")))
                .isEqualTo("mode=REPORT depth=64 schemes=[livescript, javascript] media=[text/html, application/xhtml+xml, image/svg+xml]");
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_RULES_DENIED_DATA_MEDIA_TYPES=text/html"), NONE))
                .isEqualTo("mode=REPORT depth=64 schemes=[javascript, vbscript] media=[text/html]");
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES="), NONE))
                .isEqualTo("mode=REPORT depth=64 schemes=[] media=[text/html, application/xhtml+xml, image/svg+xml]");
    }

    /**
     * Empty variables, as charts often render them: a blank mode or activation keeps its default and an empty list is
     * empty. (A blank number or true/false value is Boot's own conversion: Boot 2.7/3 reject it at startup.)
     */
    @Test
    void blankVariables() {
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_ACTIVATION= "), NONE)).isEqualTo("mode=REPORT depth=64 " + DEFAULT_RULES);
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_ACTIVATION=${RV_ACTIVATION:}"), NONE))
                .isEqualTo("mode=REPORT depth=64 " + DEFAULT_RULES);
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_ENABLED=true", "EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT=true",
                "EGOV_REQUEST_VALIDATION_MODE="), NONE, new String[0]))
                .isEqualTo("mode=REPORT depth=64 " + DEFAULT_RULES);
    }

    @Test
    void theKillSwitchAndNothingConfigured() {
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_ENABLED=false"), NONE)).isEqualTo("INACTIVE");
        assertThat(bound(NONE, NONE, new String[0])).isEqualTo("INACTIVE");
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_MODE=ENFORCE"), NONE, new String[0])).isEqualTo("INACTIVE");
    }

    @Test
    void aHostPostProcessorThatNeedsThePropertiesEarly() {
        assertThat(bound(map("EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=9"), NONE, FILE, EarlyPropertiesUser.class))
                .isEqualTo("mode=REPORT depth=9 " + DEFAULT_RULES);
    }

    /** A real application on embedded Tomcat, configured only by environment variables, plus command-line overrides. */
    @Test
    void aSpringApplication() throws Exception {
        Map<String, Object> variables = map("EGOV_REQUEST_VALIDATION_ENABLED=true",
                "EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT=${RV_STRUCTURED}", "RV_STRUCTURED=true",
                "EGOV_REQUEST_VALIDATION_ACTIVATION=",
                "EGOV_REQUEST_VALIDATION_MODE=ENFORCE", "EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH=3",
                "EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES=livescript");
        ConfigurableApplicationContext context = run(variables);
        try {
            assertThat(summary(context.getBean(RequestValidationProperties.class)))
                    .isEqualTo("mode=ENFORCE depth=3 schemes=[livescript] media=[text/html, application/xhtml+xml, image/svg+xml]");
            int port = context.getEnvironment().getProperty("local.server.port", Integer.class);
            assertThat(post(port, "{\"u\":\"livescript:x\"}")).isEqualTo(400);
            assertThat(post(port, "{\"u\":\"javascript:x\"}")).isEqualTo(200);
            assertThat(post(port, "{\"a\":{\"b\":{\"c\":{}}}}")).isEqualTo(400);
            assertThat(post(port, "{\"a\":{\"b\":{}}}")).isEqualTo(200);
        } finally {
            context.close();
        }
        variables.put("EGOV_REQUEST_VALIDATION_MODE", "REPORT");
        assertThat(setting(variables, "--egov.request-validation.mode=ENFORCE")).isEqualTo("mode=ENFORCE depth=3");
        assertThat(setting(variables, "--egov.request-validation.limits.maxDepth=7")).isEqualTo("mode=REPORT depth=7");
        // Boot 1.5 ignores a camelCase prefix (egov.requestValidation) entirely, and the library leaves a setting
        // that another source mentions to Boot, so there neither value applies and the default stays.
        boolean boot15 = ClassUtils.isPresent("org.springframework.boot.bind.RelaxedPropertyResolver", null);
        assertThat(setting(variables, "--egov.requestValidation.limits.maxDepth=7"))
                .isEqualTo(boot15 ? "mode=REPORT depth=64" : "mode=REPORT depth=7");
    }

    private static String setting(Map<String, Object> variables, String argument) {
        ConfigurableApplicationContext context = run(variables, argument);
        try {
            RequestValidationProperties properties = context.getBean(RequestValidationProperties.class);
            return "mode=" + properties.getMode() + " depth=" + properties.getLimits().getMaxDepth();
        } finally {
            context.close();
        }
    }

    private static String bound(Map<String, Object> variables, Map<String, Object> systemProperties) {
        return bound(variables, systemProperties, FILE);
    }

    private static String bound(Map<String, Object> variables, Map<String, Object> systemProperties, String[] file,
                                Class<?>... more) {
        AnnotationConfigWebApplicationContext context = context(variables, systemProperties, file, more);
        try {
            context.refresh();
            Map<String, RequestValidationProperties> beans = context.getBeansOfType(RequestValidationProperties.class);
            return beans.isEmpty() ? "INACTIVE" : summary(beans.values().iterator().next());
        } catch (RuntimeException failure) {
            Throwable cause = failure;
            while (cause.getCause() != null) cause = cause.getCause();
            return "FAILED " + cause;
        } finally {
            context.close();
        }
    }

    private static double sampleRate(Map<String, Object> variables) {
        AnnotationConfigWebApplicationContext context = context(variables, NONE, FILE);
        try {
            context.refresh();
            return context.getBean(RequestValidationProperties.class).getLog().getReportSampleRate();
        } finally {
            context.close();
        }
    }

    /** The property sources of a service: system properties, then OS environment, then application.properties. */
    private static AnnotationConfigWebApplicationContext context(Map<String, Object> variables,
                                                                 Map<String, Object> systemProperties, String[] file,
                                                                 Class<?>... more) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        MutablePropertySources sources = context.getEnvironment().getPropertySources();
        sources.replace(SYS, new MapPropertySource(SYS, systemProperties));
        sources.replace(ENV, new SystemEnvironmentPropertySource(ENV, variables));
        sources.addLast(new MapPropertySource("applicationConfig: [classpath:/application.properties]", map(file)));
        attach(context.getEnvironment());
        context.register(Web.class, AutoConfiguration.class);
        if (more.length > 0) context.register(more);
        return context;
    }

    // Boot 2+: as SpringApplication does before refresh.
    private static void attach(ConfigurableEnvironment environment) {
        try {
            Class.forName("org.springframework.boot.context.properties.source.ConfigurationPropertySources")
                    .getMethod("attach", org.springframework.core.env.Environment.class).invoke(null, environment);
        } catch (ClassNotFoundException boot1) {
            // Boot 1.5 has no attached view.
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static ConfigurableApplicationContext run(Map<String, Object> variables, String... arguments) {
        List<Class<?>> sources = new ArrayList<Class<?>>();
        for (String name : new String[] {
                "org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration",
                "org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration",
                "org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration",
                "org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration",
                "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration",
                "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration"}) {
            try {
                sources.add(Class.forName(name));
            } catch (ClassNotFoundException otherBootLine) {
                // Each name exists on one Boot line only.
            }
        }
        assertThat(sources).hasSizeGreaterThanOrEqualTo(3);
        sources.add(Web.class);
        sources.add(AutoConfiguration.class);
        StandardServletEnvironment environment = new StandardServletEnvironment();
        environment.getPropertySources().replace(ENV, new SystemEnvironmentPropertySource(ENV, new HashMap<String, Object>(variables)));
        environment.getPropertySources().replace(SYS, new MapPropertySource(SYS, NONE));
        SpringApplication application = new SpringApplication(sources.toArray(new Class<?>[0]));
        application.setEnvironment(environment);
        application.setDefaultProperties(map("server.port=0", "spring.main.banner-mode=off"));
        return application.run(arguments);
    }

    private static int post(int port, String json) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + port + "/bytes").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        OutputStream body = connection.getOutputStream();
        try {
            body.write(json.getBytes(StandardCharsets.UTF_8));
        } finally {
            body.close();
        }
        int status = connection.getResponseCode();
        connection.disconnect();
        return status;
    }

    private static String summary(RequestValidationProperties properties) {
        return "mode=" + properties.getMode() + " depth=" + properties.getLimits().getMaxDepth()
                + " schemes=" + properties.getRules().toPolicy().getDeniedSchemes()
                + " media=" + properties.getRules().toPolicy().getDeniedDataMediaTypes();
    }

    private static String[] plus(String[] base, String... more) {
        String[] all = new String[base.length + more.length];
        System.arraycopy(base, 0, all, 0, base.length);
        System.arraycopy(more, 0, all, base.length, more.length);
        return all;
    }

    private static Map<String, Object> map(String... entries) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (String entry : entries) map.put(entry.substring(0, entry.indexOf('=')), entry.substring(entry.indexOf('=') + 1));
        return map;
    }

    @Configuration
    @ImportAutoConfiguration(RequestValidationAutoConfiguration.class)
    static class AutoConfiguration { }

    @Configuration
    @EnableWebMvc
    static class Web {
        @Bean Endpoint endpoint() { return new Endpoint(); }
        @Bean ErrorAdvice errorAdvice() { return new ErrorAdvice(); }
    }

    @RestController
    @ValidateRequest
    static class Endpoint {
        @PostMapping("/bytes") public byte[] bytes(@RequestBody byte[] body) { return body; }
    }

    @RestControllerAdvice
    static class ErrorAdvice {
        @ExceptionHandler(ContentPolicyViolationException.class)
        ResponseEntity<String> reject(ContentPolicyViolationException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getCode());
        }
    }

    /** A host post-processor (Ordered) with a dependency on the properties, so they are created early. */
    @Configuration
    static class EarlyPropertiesUser {
        @Bean static OrderedPostProcessor orderedPostProcessor(RequestValidationProperties properties) {
            return new OrderedPostProcessor();
        }
    }

    static class OrderedPostProcessor implements BeanPostProcessor, Ordered {
        @Override public Object postProcessBeforeInitialization(Object bean, String name) { return bean; }
        @Override public Object postProcessAfterInitialization(Object bean, String name) { return bean; }
        @Override public int getOrder() { return 0; }
    }
}
