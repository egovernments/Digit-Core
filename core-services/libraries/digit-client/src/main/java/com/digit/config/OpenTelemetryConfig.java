package com.digit.config;

import com.digit.interceptor.OpenTelemetryRestTemplateInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.ResourceAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * OpenTelemetry configuration for digit-client library.
 * Provides automatic tracing for all HTTP calls made via RestTemplate.
 */
@Slf4j
@Configuration
@ConditionalOnClass(OpenTelemetry.class)
@ConditionalOnProperty(
        prefix = "digit.opentelemetry",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
@EnableConfigurationProperties(OpenTelemetryProperties.class)
@RequiredArgsConstructor
public class OpenTelemetryConfig {

    private final OpenTelemetryProperties properties;

    /**
     * Configure OpenTelemetry SDK with OTLP exporter
     */
    @Bean
    public OpenTelemetry openTelemetry() {
        log.info("Initializing OpenTelemetry with endpoint: {}", properties.getEndpoint());

        // Create resource with service name
        Resource resource = Resource.getDefault()
                .merge(Resource.create(
                        Attributes.builder()
                                .put(ResourceAttributes.SERVICE_NAME, properties.getServiceName())
                                .build()
                ));

        // Configure OTLP exporter
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(properties.getEndpoint())
                .setTimeout(Duration.ofMillis(properties.getExportTimeout()))
                .build();

        // Configure tracer provider with batch processor
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(
                        BatchSpanProcessor.builder(spanExporter)
                                .setMaxExportBatchSize(properties.getBatchSize())
                                .build()
                )
                .setSampler(Sampler.traceIdRatioBased(properties.getSamplingRatio()))
                .build();

        // Build OpenTelemetry SDK
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down OpenTelemetry...");
            tracerProvider.close();
        }));

        log.info("OpenTelemetry initialized successfully for service: {}", properties.getServiceName());
        return openTelemetry;
    }

    /**
     * Create a tracer instance for the digit-client library
     */
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        log.info("Creating OpenTelemetry tracer for digit-client");
        return openTelemetry.getTracer("digit-client", "1.0.0");
    }

    /**
     * Bean to create OpenTelemetry RestTemplate interceptor
     */
    @Bean
    public OpenTelemetryRestTemplateInterceptor openTelemetryRestTemplateInterceptor(
            Tracer tracer, OpenTelemetry openTelemetry) {
        log.info("Creating OpenTelemetry RestTemplate interceptor");
        return new OpenTelemetryRestTemplateInterceptor(tracer, openTelemetry);
    }
}
