package org.digit.config;

import org.digit.exception.DigitClientErrorHandler;
import org.digit.interceptor.OpenTelemetryRestTemplateInterceptor;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ComponentScan(basePackages={"org.egov"})
@Slf4j
@RequiredArgsConstructor
public class ApiConfig {
    private final ApiProperties apiProperties;
    @Autowired(required=false)
    private OpenTelemetryRestTemplateInterceptor openTelemetryInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(this.apiProperties.getConnectTimeout());
        factory.setReadTimeout(this.apiProperties.getReadTimeout());
        restTemplate.setRequestFactory((ClientHttpRequestFactory)factory);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(this.objectMapper());
        restTemplate.getMessageConverters().add(0, converter);
        if (this.openTelemetryInterceptor != null) {
            try {
                List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
                interceptors.add(this.openTelemetryInterceptor);
                restTemplate.setInterceptors(interceptors);
                log.info("\u2705 OpenTelemetry tracing enabled for RestTemplate");
            }
            catch (Exception e) {
                log.warn("\u26a0\ufe0f Failed to enable OpenTelemetry tracing: {}", (Object)e.getMessage());
            }
        } else {
            log.info("\u2139\ufe0f OpenTelemetry tracing disabled (digit.opentelemetry.enabled=false or not configured)");
        }
        log.info("\u2705 RestTemplate created - interceptors will be added by auto-configuration");
        restTemplate.setErrorHandler((ResponseErrorHandler)new DigitClientErrorHandler());
        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule((Module)new JavaTimeModule());
        return mapper;
    }
}