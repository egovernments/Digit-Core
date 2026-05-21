package org.digit.config;

import io.opentelemetry.api.OpenTelemetry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import lombok.extern.slf4j.Slf4j;

@AutoConfiguration
@ConditionalOnClass(value={OpenTelemetry.class})
@ConditionalOnProperty(prefix="digit.opentelemetry", name={"enabled"}, havingValue="true", matchIfMissing=false)
@EnableConfigurationProperties(value={OpenTelemetryProperties.class})
@Import(value={OpenTelemetryConfig.class})
@Slf4j
public class OpenTelemetryAutoConfiguration {

    public OpenTelemetryAutoConfiguration() {
        log.info("\ud83d\udd0d OpenTelemetry Auto-Configuration activated for digit-client");
    }
}