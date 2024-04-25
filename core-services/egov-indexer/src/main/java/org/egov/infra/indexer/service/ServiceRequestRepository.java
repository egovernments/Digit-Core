package org.egov.infra.indexer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

	public String fetchResult(String uri, Object request, String tenantId)  {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String response = null;
		log.info("URI: "+uri.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.set(TENANTID_MDC_STRING, tenantId);

		// Create an HttpEntity with headers (if any)
		HttpEntity<?> requestEntity = new HttpEntity<>(request, headers);
		ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<String>() {};

		// Make the HTTP request using the exchange method
		ResponseEntity<String>  responseEntity = null;

		try {
			log.info("Request: "+mapper.writeValueAsString(request));
			responseEntity = restTemplate.exchange(
					uri.toString(),
					HttpMethod.POST,
					requestEntity,
					responseType
			);

		}catch(HttpClientErrorException e) {
			log.error("External Service threw an Exception: ",e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		}catch(Exception e) {
			log.error("Exception while fetching from searcher: ",e);
		}

		// Extract the JSON content from the ResponseEntity
		response = responseEntity.getBody();

		return response;
	}

}
