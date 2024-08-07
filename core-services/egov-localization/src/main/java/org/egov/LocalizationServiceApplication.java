package org.egov;

import co.elastic.apm.attach.ElasticApmAttacher;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@SpringBootApplication
@Import({TracerConfiguration.class})
public class LocalizationServiceApplication {

    @Value("${app.timezone}")
    private String timeZone;

    @Value("${elastic.apm.service-name}")
    private String serviceName;

    @Value("${elastic.apm.server-url}")
    private String serviceUrl;

    @Value("${elastic.apm.application-packages}")
    private String applicationPackages;

    @Value("${elastic.apm.environment}")
    private String environment;

    public static void main(String[] args) {
        SpringApplication.run(LocalizationServiceApplication.class, args);
    }

    @PostConstruct
    public void initElasticApm() {
        Map<String, String> apmConfig = new HashMap<>();
        apmConfig.put("service_name", serviceName);
        apmConfig.put("server_urls", serviceUrl);
        apmConfig.put("secret_token", "");
        apmConfig.put("application_packages", applicationPackages);
        apmConfig.put("environment", environment);
        ElasticApmAttacher.attach(apmConfig);
    }

    @PostConstruct
    public void initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
                configurer.defaultContentType(MediaType.APPLICATION_JSON_UTF8);
            }
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
