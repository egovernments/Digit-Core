package org.egov;

import co.elastic.apm.attach.ElasticApmAttacher;
import org.springframework.boot.SpringApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class UrlSorteningApplication {

    @Value("${elastic.apm.service-name}")
    private String serviceName;

    @Value("${elastic.apm.server-url}")
    private String serviceUrl;

    @Value("${elastic.apm.application-packages}")
    private String applicationPackages;

    @Value("${elastic.apm.environment}")
    private String environment;

	public static void main(String[] args) {
		SpringApplication.run(UrlSorteningApplication.class, args);
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

	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}
}
