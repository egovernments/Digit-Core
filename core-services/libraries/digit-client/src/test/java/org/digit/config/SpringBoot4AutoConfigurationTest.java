package org.digit.config;

import org.digit.services.billing.BillingClient;
import org.digit.services.boundary.BoundaryClient;
import org.digit.services.filestore.FilestoreClient;
import org.digit.services.idgen.IdGenClient;
import org.digit.services.individual.IndividualClient;
import org.digit.services.mdms.MdmsClient;
import org.digit.services.notification.NotificationClient;
import org.digit.services.registry.RegistryClient;
import org.digit.services.workflow.WorkflowClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves that the digit-client library is correctly configured for Spring Boot 4.
 *
 * Three things are verified:
 *   1. The Spring context loads cleanly under Spring Framework 7 — all 10 service
 *      client beans and the RestTemplate are created with no errors.
 *   2. Header propagation wiring — the interceptor is attached to the RestTemplate
 *      by the BeanPostProcessor in HeaderPropagationAutoConfiguration.
 *   3. Spring Boot 4 auto-configuration registration — the AutoConfiguration.imports
 *      file is present and lists both auto-configuration classes. Spring Boot 4
 *      dropped spring.factories; only this file is used.
 */
@DisplayName("Spring Boot 4 Auto-Configuration Verification")
class SpringBoot4AutoConfigurationTest {

    static AnnotationConfigApplicationContext ctx;

    @BeforeAll
    static void startContext() {
        // ApiConfig provides RestTemplate + ObjectMapper.
        // HeaderPropagationAutoConfiguration creates all 10 service clients and
        // wires the header propagation interceptor.
        // ApiProperties @Value fields use their declared defaults — no property
        // source needed.
        ctx = new AnnotationConfigApplicationContext(
                ApiConfig.class,
                HeaderPropagationAutoConfiguration.class
        );
    }

    @AfterAll
    static void stopContext() {
        if (ctx != null) ctx.close();
    }

    // ── Bean presence ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("RestTemplate bean is created")
    void restTemplateIsCreated() {
        assertNotNull(ctx.getBean(RestTemplate.class));
    }

    @Test
    @DisplayName("IdGenClient bean is auto-configured")
    void idGenClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(IdGenClient.class));
    }

    @Test
    @DisplayName("WorkflowClient bean is auto-configured")
    void workflowClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(WorkflowClient.class));
    }

    @Test
    @DisplayName("BillingClient bean is auto-configured")
    void billingClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(BillingClient.class));
    }

    @Test
    @DisplayName("BoundaryClient bean is auto-configured")
    void boundaryClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(BoundaryClient.class));
    }

    @Test
    @DisplayName("NotificationClient bean is auto-configured")
    void notificationClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(NotificationClient.class));
    }

    @Test
    @DisplayName("RegistryClient bean is auto-configured")
    void registryClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(RegistryClient.class));
    }

    @Test
    @DisplayName("MdmsClient bean is auto-configured")
    void mdmsClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(MdmsClient.class));
    }

    @Test
    @DisplayName("IndividualClient bean is auto-configured")
    void individualClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(IndividualClient.class));
    }

    @Test
    @DisplayName("FilestoreClient bean is auto-configured")
    void filestoreClientIsAutoConfigured() {
        assertNotNull(ctx.getBean(FilestoreClient.class));
    }

    // ── Header propagation wiring ─────────────────────────────────────────────

    @Test
    @DisplayName("HeaderPropagationInterceptor is attached to RestTemplate by the BeanPostProcessor")
    void headerInterceptorIsAttachedToRestTemplate() {
        RestTemplate restTemplate = ctx.getBean(RestTemplate.class);
        boolean attached = restTemplate.getInterceptors().stream()
                .anyMatch(i -> i instanceof HeaderPropagationInterceptor);
        assertTrue(attached,
                "HeaderPropagationInterceptor must be added to the RestTemplate by " +
                "HeaderPropagationAutoConfiguration's BeanPostProcessor");
    }

    // ── Spring Boot 4 registration file ──────────────────────────────────────

    @Test
    @DisplayName("AutoConfiguration.imports is present (Spring Boot 4 discovery mechanism)")
    void autoConfigurationImportsFileIsPresent() {
        URL resource = getClass().getClassLoader().getResource(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
        assertNotNull(resource,
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports " +
                "must be present — Spring Boot 4 uses this file exclusively for auto-configuration discovery");
    }

    @Test
    @DisplayName("HeaderPropagationAutoConfiguration is registered in AutoConfiguration.imports")
    void headerPropagationAutoConfigurationIsRegistered() throws Exception {
        List<String> imports = readAutoConfigurationImports();
        assertTrue(imports.contains("org.digit.config.HeaderPropagationAutoConfiguration"),
                "HeaderPropagationAutoConfiguration must appear in AutoConfiguration.imports");
    }

    @Test
    @DisplayName("RegistryCacheAutoConfiguration is registered in AutoConfiguration.imports")
    void registryCacheAutoConfigurationIsRegistered() throws Exception {
        List<String> imports = readAutoConfigurationImports();
        assertTrue(imports.contains("org.digit.config.RegistryCacheAutoConfiguration"),
                "RegistryCacheAutoConfiguration must appear in AutoConfiguration.imports");
    }

    @Test
    @DisplayName("spring.factories is absent — Spring Boot 4 does not use it for auto-configuration")
    void legacySpringFactoriesDoesNotContainAutoConfiguration() throws Exception {
        // Spring Boot's own jars may have spring.factories for non-autoconfig purposes,
        // so we verify that *our* classes are not registered there.
        URL resource = getClass().getClassLoader().getResource("META-INF/spring.factories");
        if (resource == null) return; // no spring.factories at all — pass

        try (var is = resource.openStream();
             var reader = new BufferedReader(new InputStreamReader(is))) {
            String content = reader.lines()
                    .filter(l -> !l.startsWith("#"))
                    .reduce("", (a, b) -> a + "\n" + b);
            assertFalse(content.contains("org.digit.config.HeaderPropagationAutoConfiguration"),
                    "HeaderPropagationAutoConfiguration must not be in spring.factories; " +
                    "Spring Boot 4 ignores EnableAutoConfiguration entries there");
            assertFalse(content.contains("org.digit.config.RegistryCacheAutoConfiguration"),
                    "RegistryCacheAutoConfiguration must not be in spring.factories");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> readAutoConfigurationImports() throws Exception {
        URL resource = getClass().getClassLoader().getResource(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
        assertNotNull(resource, "AutoConfiguration.imports file must exist");
        try (var is = resource.openStream();
             var reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .toList();
        }
    }
}
