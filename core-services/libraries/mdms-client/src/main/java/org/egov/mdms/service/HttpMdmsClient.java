package org.egov.mdms.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(name = "egov.mdms.mode", havingValue = "http", matchIfMissing = true)
public class HttpMdmsClient implements MdmsClient {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${mdms.service.host:http://localhost:8080/}")
	private String mdmsHost;

	@Value("${mdms.service.search.uri:egov-mdms-service/v1/_search}")
	private String mdmsSearchUri;

	@PostConstruct
	void logActivation() {
		log.info("HttpMdmsClient active (egov.mdms.mode=http). Target: {}{}", mdmsHost, mdmsSearchUri);
	}

	@Override
	public MdmsResponse getMaster(RequestInfo requestInfo, String tenantId,
			Map<String, List<MasterDetail>> masterDetails) {
		log.info("HttpMdmsClient masterDetails: {}", masterDetails);
		MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
		mdmsCriteriaReq.setRequestInfo(requestInfo);
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		for (Map.Entry<String, List<MasterDetail>> entry : masterDetails.entrySet()) {
			ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(entry.getKey())
					.masterDetails(entry.getValue()).build();

			moduleDetails.add(moduleDetail);

			MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
			mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
		}
		return getMaster(mdmsCriteriaReq);
	}

	@Override
	public MdmsResponse getMaster(MdmsCriteriaReq mdmsCriteriaReq) {
		log.info("mdmsCriteriaReq: {}", mdmsCriteriaReq);
		MdmsResponse mdmsResponse = null;
		try {
			mdmsResponse = restTemplate.postForObject(mdmsHost.concat(mdmsSearchUri), mdmsCriteriaReq,
					MdmsResponse.class);
		} catch (HttpClientErrorException ex) {
			ex.printStackTrace();
			String excep = ex.getResponseBodyAsString();
			log.info("HttpClientErrorException: {}", excep);
			throw new ServiceCallException(excep);
		} catch (Exception ex) {
			log.error("Exception: {}", ex.getMessage());
			throw new CustomException("MDMS_RESPONSE_ERROR",
					"Error while fetching data from MDMS: " + ex.getMessage());
		}
		return mdmsResponse;
	}
}
