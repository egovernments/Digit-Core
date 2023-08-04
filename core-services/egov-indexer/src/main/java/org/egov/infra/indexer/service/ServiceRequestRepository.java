package org.egov.infra.indexer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.egov.infra.indexer.util.IndexerConstants.TENANTID_MDC_STRING;

@Repository
@Slf4j
public class ServiceRequestRepository {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;

	public Object fetchResult(StringBuilder uri, Object request, String tenantId) {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		log.info("URI: "+uri.toString());

		//This block of code is only for central instance purposes
		HttpHeaders headers = new HttpHeaders();
		headers.set(TENANTID_MDC_STRING,tenantId);

		HttpEntity<String> requestEntity = null;
		if(request != null )
			requestEntity = new HttpEntity<>(request.toString(), headers);
		else requestEntity = new HttpEntity<>("{}", headers);

		try {
			log.info("Request: "+mapper.writeValueAsString(request));
			response = restTemplate.postForEntity(uri.toString(), requestEntity, Map.class);
		}catch(HttpClientErrorException e) {
			log.error("External Service threw an Exception: ",e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		}catch(Exception e) {
			log.error("Exception while fetching from searcher: ",e);
		}

		return response;
	}
}
