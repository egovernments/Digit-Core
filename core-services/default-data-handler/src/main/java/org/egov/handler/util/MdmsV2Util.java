package org.egov.handler.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.DefaultMdmsDataRequest;
import org.egov.handler.web.models.MdmsCriteriaReqV2;
import org.egov.handler.web.models.MdmsResponseV2;
import org.egov.tracer.model.CustomException;
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

	public void createMdmsData(DefaultMdmsDataRequest defaultMdmsDataRequest) {

		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getMdmsDefaultDataCreateURI());
		try {
			restTemplate.postForObject(uri.toString(), defaultMdmsDataRequest, ResponseInfo.class);
		} catch (Exception e) {
			log.error("Error creating default MDMS data for {}", defaultMdmsDataRequest.getTargetTenantId());
		}
	}

	public MdmsResponseV2 searchMdmsData(MdmsCriteriaReqV2 mdmsCriteriaReqV2) {
		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getMdmsDataSearchURI());
		try {
			return restTemplate.postForObject(uri.toString(), mdmsCriteriaReqV2, MdmsResponseV2.class);
		} catch (Exception e) {
			log.error("Error searching MDMS data for {}", mdmsCriteriaReqV2.getMdmsCriteria().getTenantId());
			throw new CustomException("MDMS_DATA_SEARCH_FAILED", "Failed to search mdms data " + mdmsCriteriaReqV2.getMdmsCriteria().getTenantId());
		}
	}
}
