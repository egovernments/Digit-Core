package org.egov.handler.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.BusinessServiceRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class WorkflowUtil {

	private final RestTemplate restTemplate;

	private final ServiceConfiguration serviceConfig;

	@Autowired
	public WorkflowUtil(RestTemplate restTemplate, ServiceConfiguration serviceConfig) {
		this.restTemplate = restTemplate;
		this.serviceConfig = serviceConfig;
	}

	public void createWfConfig(BusinessServiceRequest businessServiceRequest) {

		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getWfBusinessServiceCreateURI());
		try {
			restTemplate.postForObject(uri.toString(), businessServiceRequest, Map.class);
		} catch (Exception e) {
			log.error("Error creating workflow configuration: {}", e.getMessage());
			throw new CustomException("WF_CONFIG_CREATE_FAILED", "Failed to create workflow configuration: " + e.getMessage());
		}
	}
}
