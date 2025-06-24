package org.egov.pgr.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.web.models.RequestInfoWrapper;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class ServiceRequestRepository {

	private ObjectMapper mapper;

	private RestTemplate restTemplate;

	@Autowired
	public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate) {
		this.mapper = mapper;
		this.restTemplate = restTemplate;
	}

	/**
	 * Fetches results from a REST service using the uri and object
	 *
	 * @param uri
	 * @param request
	 * @return
	 */
	public Object fetchResult(StringBuilder uri, Object request) {
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(new RequestInfo()).build();
		if(request instanceof RequestInfoWrapper)
			requestInfoWrapper = (RequestInfoWrapper) request;
		else if(request instanceof ServiceRequest)
			requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(((ServiceRequest) request).getRequestInfo()).build();

		Object response = null;
		try {
			if (uri.toString().contains("/engine-rest/task")) {
				response = restTemplate.postForObject(uri.toString(), request, List.class);
			} else {
				response = restTemplate.postForObject(uri.toString(), request, Map.class);
			}
			log.info("Raw External Service Response: " + mapper.writeValueAsString(response));
		}catch(HttpClientErrorException e) {
			log.error("External Service threw an Exception: ",e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		}catch(Exception e) {
			log.error("Exception while fetching from searcher: ",e);
		}

		return response;
	}
}
