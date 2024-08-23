package org.egov.handler.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.DefaultMasterDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class MdmsV2Util {

	private final RestTemplate restTemplate;

	private final ServiceConfiguration serviceConfig;

	@Autowired
	public MdmsV2Util(RestTemplate restTemplate, ServiceConfiguration serviceConfig) {
		this.restTemplate = restTemplate;
		this.serviceConfig = serviceConfig;
	}

	public void createMdmsData(DefaultMasterDataRequest defaultMasterDataRequest) {

		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getMdmsDefaultDataCreateURI());
		try {
			ResponseInfo responseInfo = restTemplate.postForObject(uri.toString(), defaultMasterDataRequest, ResponseInfo.class);

		} catch (Exception e) {
			log.error("Error creating default MDMS data for {}", defaultMasterDataRequest.getTargetTenantId());
		}

	}
}
