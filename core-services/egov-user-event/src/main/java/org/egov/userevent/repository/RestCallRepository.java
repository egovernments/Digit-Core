package org.egov.userevent.repository;

import java.util.Map;
import java.util.Optional;

import org.egov.tracer.model.ServiceCallException;
import org.egov.userevent.web.context.HeaderNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class RestCallRepository {

	@Autowired
	private RestTemplate restTemplate;
		
	/**
	 * Fetches results from a REST service using the uri and object
	 * 
	 * @param requestInfo
	 * @param serviceReqSearchCriteria
	 * @return Optional
	 * @author vishal
	 */
	public Optional<Object> fetchResult(StringBuilder uri, Object request) {
		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		try {
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		}catch(HttpClientErrorException e) {
			log.error("External Service threw an Exception: ",e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		}catch(Exception e) {
			log.error("Exception while fetching data: ",e);
		}
		return Optional.ofNullable(response);

	}

	/**
	 * GET against a header-authenticated 3.0-style service (e.g. mdms-v2 /v2):
	 * tenant via X-Tenant-ID, client id via X-Client-ID, and the caller's
	 * bearer token forwarded on the Authorization header.
	 */
	public Optional<Object> fetchResultWithHeaders(StringBuilder uri, String tenantId, String clientId,
			String authToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HeaderNames.TENANT_ID, tenantId);
		if (StringUtils.hasText(clientId)) {
			headers.set("X-Client-ID", clientId);
		}
		if (StringUtils.hasText(authToken)) {
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
		}
		Object response = null;
		try {
			response = restTemplate.exchange(uri.toString(), HttpMethod.GET, new HttpEntity<Void>(headers), Map.class)
					.getBody();
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Exception while fetching data: ", e);
		}
		return Optional.ofNullable(response);
	}

}
