package org.egov.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RoutingConfig {

	@Value("${egov.service.config.path}")
	private String serviceConfigPath;

	private final ResourceLoader resourceLoader;

	private Map<String, Map<String, String>> tenantRoutingConfigWrapper;

	public RoutingConfig(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@PostConstruct
	public void loadServiceConfigurationYaml() {

		log.info("Translator Service Reading Configuration from tenant-config given in path : " + serviceConfigPath);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Resource resource = resourceLoader.getResource(serviceConfigPath);
			try (InputStream is = resource.getInputStream()) {
				tenantRoutingConfigWrapper = mapper.readValue(is,
						new TypeReference<Map<String, Map<String, String>>>() {});
			}
			log.info("Loaded tenant routing config: " + tenantRoutingConfigWrapper.toString());
		} catch (IOException e) {
			log.error("Failed to load tenant routing config from: " + serviceConfigPath, e);
		}
	}

	public Map<String, Map<String, String>> getTenantRoutingConfigWrapper() {
		return tenantRoutingConfigWrapper;
	}

}
