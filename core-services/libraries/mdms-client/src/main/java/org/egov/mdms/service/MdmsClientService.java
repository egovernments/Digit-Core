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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MdmsClientService {

	private final WebClient webClient;

	@Value("${mdms.service.host:http://localhost:8080/}")
	private String mdmsHost;

	@Value("${mdms.service.search.uri:egov-mdms-service/v1/_search}")
	private String mdmsSearchUri;

	@Autowired
	public MdmsClientService(@Qualifier("logAwareWebClient") WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.build();
	}

	public MdmsResponse getMaster(RequestInfo requestInfo, String tenantId,
			Map<String, List<MasterDetail>> masterDetails) {
		log.info("MdmsClientService masterDetails:" + masterDetails);
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

	public MdmsResponse getMaster(MdmsCriteriaReq mdmsCriteriaReq) {
		log.info("mdmsCriteriaReq:" + mdmsCriteriaReq);
		try {
			return webClient.post()
					.uri(mdmsHost + mdmsSearchUri)
					.bodyValue(mdmsCriteriaReq)
					.retrieve()
					.bodyToMono(MdmsResponse.class)
					.block();
		} catch (WebClientResponseException ex) {
			String excep = ex.getResponseBodyAsString();
			log.info("WebClientResponseException:" + excep);
			throw new ServiceCallException(excep);
		} catch (Exception ex) {
			log.error("Exception: " + ex.getMessage());
			throw new CustomException("MDMS_RESPONSE_ERROR", "Error while fetching data from MDMS: " + ex.getMessage());
		}
	}
}
