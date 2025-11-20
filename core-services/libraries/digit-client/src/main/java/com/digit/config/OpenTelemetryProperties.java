package com.digit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OpenTelemetry tracing.
 * Can be configured via application.properties or environment variables.
 */
@Data
@ConfigurationProperties(prefix = "digit.opentelemetry")
public class OpenTelemetryProperties {

    /**
     * Enable/disable OpenTelemetry tracing
     */
    private boolean enabled = false;

    /**
     * Service name for tracing
     */
    private String serviceName = "digit-client";

    /**
     * OTLP exporter endpoint (e.g., http://localhost:4317)
     */
    private String endpoint = "http://localhost:4317";

    /**
     * Sampling ratio (0.0 to 1.0)
     */
    private double samplingRatio = 1.0;

    /**
     * Enable detailed logging of traces
     */
    private boolean detailedLogging = false;

    /**
     * Export timeout in milliseconds
     */
    private long exportTimeout = 30000;

    /**
     * Batch size for span export
     */
    private int batchSize = 512;
}
