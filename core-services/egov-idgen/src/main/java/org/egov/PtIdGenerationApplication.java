package org.egov;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Description : This is initialization class for pt-idGeneration module
 *
 * @author Pavan Kumar Kamma
 *
 */
@SpringBootApplication
@Import({TracerConfiguration.class})
public class PtIdGenerationApplication {
	public static void main(String[] args) {
		SpringApplication.run(PtIdGenerationApplication.class, args);
	}

	@Bean
	public OtlpGrpcSpanExporter otlpHttpSpanExporter(@Value("${tracing.url}") String url) {
		return OtlpGrpcSpanExporter.builder().setEndpoint(url).build();
	}
}
