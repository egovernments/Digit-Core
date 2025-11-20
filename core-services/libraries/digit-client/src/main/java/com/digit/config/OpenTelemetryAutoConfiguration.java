package com.digit.config;

import io.opentelemetry.api.OpenTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot Auto-Configuration for OpenTelemetry in digit-client library.
 * Automatically enables tracing when OpenTelemetry is on the classpath and enabled in configuration.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(OpenTelemetry.class)
@ConditionalOnProperty(
        prefix = "digit.opentelemetry",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
@EnableConfigurationProperties(OpenTelemetryProperties.class)
@Import(OpenTelemetryConfig.class)
public class OpenTelemetryAutoConfiguration {

    public OpenTelemetryAutoConfiguration() {
        log.info("🔍 OpenTelemetry Auto-Configuration activated for digit-client");
    }
}
