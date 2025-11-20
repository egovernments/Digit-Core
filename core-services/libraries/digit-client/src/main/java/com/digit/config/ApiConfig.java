package com.digit.config;

import com.digit.exception.DigitClientErrorHandler;
import com.digit.interceptor.OpenTelemetryRestTemplateInterceptor;
import com.digit.util.HeaderStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Configuration class for Digit client library.
 * Defines RestTemplate bean with timeout and retry configuration.
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.digit")
@RequiredArgsConstructor
public class ApiConfig {

    private final ApiProperties apiProperties;

    @Autowired(required = false)
    private OpenTelemetryRestTemplateInterceptor openTelemetryInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Set timeouts using SimpleClientHttpRequestFactory
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(apiProperties.getConnectTimeout());
        factory.setReadTimeout(apiProperties.getReadTimeout());
        restTemplate.setRequestFactory(factory);
        
        // Configure JSON message converter
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        restTemplate.getMessageConverters().add(0, converter);
        
        // Add OpenTelemetry interceptor if available
        if (openTelemetryInterceptor != null) {
            try {
                List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
                interceptors.add(openTelemetryInterceptor);
                restTemplate.setInterceptors(interceptors);
                log.info("✅ OpenTelemetry tracing enabled for RestTemplate");
            } catch (Exception e) {
                log.warn("⚠️ Failed to enable OpenTelemetry tracing: {}", e.getMessage());
            }
        } else {
            log.info("ℹ️ OpenTelemetry tracing disabled (digit.opentelemetry.enabled=false or not configured)");
        }
        
        log.info("✅ RestTemplate created - interceptors will be added by auto-configuration");
        
        // Set custom error handler
        restTemplate.setErrorHandler(new DigitClientErrorHandler());
        
        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
