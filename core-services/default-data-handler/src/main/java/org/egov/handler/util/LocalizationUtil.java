package org.egov.handler.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.DefaultLocalizationDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class LocalizationUtil {

	private final RestTemplate restTemplate;

	private final ServiceConfiguration serviceConfig;

	@Autowired
	public LocalizationUtil(RestTemplate restTemplate, ServiceConfiguration serviceConfig) {
		this.restTemplate = restTemplate;
		this.serviceConfig = serviceConfig;
	}

	public void createLocalizationData(DefaultLocalizationDataRequest defaultLocalizationDataRequest) {

		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getLocalizationDefaultDataCreateURI());
		try {
			ResponseInfo responseInfo = restTemplate.postForObject(uri.toString(), defaultLocalizationDataRequest, ResponseInfo.class);

		} catch (Exception e) {
			log.error("Error creating default localization data for {}", defaultLocalizationDataRequest.getTargetTenantId());
		}

	}
}
