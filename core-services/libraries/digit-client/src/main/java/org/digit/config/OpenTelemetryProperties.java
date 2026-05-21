package org.digit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties(prefix="digit.opentelemetry")
@Data
public class OpenTelemetryProperties {
    private boolean enabled = false;
    private String serviceName = "digit-client";
    private String endpoint = "http://localhost:4317";
    private double samplingRatio = 1.0;
    private boolean detailedLogging = false;
    private long exportTimeout = 30000L;
    private int batchSize = 512;
}