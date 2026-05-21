package org.digit.config;

import org.digit.interceptor.OpenTelemetryRestTemplateInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.ResourceAttributes;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnClass(value={OpenTelemetry.class})
@ConditionalOnProperty(prefix="digit.opentelemetry", name={"enabled"}, havingValue="true", matchIfMissing=false)
@EnableConfigurationProperties(value={OpenTelemetryProperties.class})
@Slf4j
@RequiredArgsConstructor
public class OpenTelemetryConfig {
    private final OpenTelemetryProperties properties;

    @Bean
    public OpenTelemetry openTelemetry() {
        log.info("Initializing OpenTelemetry with endpoint: {}", (Object)this.properties.getEndpoint());
        Resource resource = Resource.getDefault().merge(Resource.create(Attributes.builder().put(ResourceAttributes.SERVICE_NAME, this.properties.getServiceName()).build()));
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder().setEndpoint(this.properties.getEndpoint()).setTimeout(Duration.ofMillis(this.properties.getExportTimeout())).build();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().setResource(resource).addSpanProcessor((SpanProcessor)BatchSpanProcessor.builder((SpanExporter)spanExporter).setMaxExportBatchSize(this.properties.getBatchSize()).build()).setSampler(Sampler.traceIdRatioBased((double)this.properties.getSamplingRatio())).build();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).setPropagators(ContextPropagators.create((TextMapPropagator)W3CTraceContextPropagator.getInstance())).build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down OpenTelemetry...");
            tracerProvider.close();
        }));
        log.info("OpenTelemetry initialized successfully for service: {}", (Object)this.properties.getServiceName());
        return openTelemetry;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        log.info("Creating OpenTelemetry tracer for digit-client");
        return openTelemetry.getTracer("digit-client", "1.0.0");
    }

    @Bean
    public OpenTelemetryRestTemplateInterceptor openTelemetryRestTemplateInterceptor(Tracer tracer, OpenTelemetry openTelemetry) {
        log.info("Creating OpenTelemetry RestTemplate interceptor");
        return new OpenTelemetryRestTemplateInterceptor(tracer, openTelemetry);
    }
}