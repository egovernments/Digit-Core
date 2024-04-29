package org.egov.inbox.repository;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.egov.inbox.util.ESAuthUtil;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ServiceRequestRepository {
	private ObjectMapper mapper;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ESAuthUtil esAuthUtil;

	@Autowired
	public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate, ESAuthUtil esAuthUtil) {
		this.mapper = mapper;
		this.restTemplate = restTemplate;
		this.esAuthUtil = esAuthUtil;
	}
	/**
	 * fetchResult form the different services based on the url and request object
	 * @param uri
	 * @param request
	 * @return
	 */
	public Object fetchResult(StringBuilder uri, Object request) {
		Object response = null;
		//log.debug("URI: " + uri.toString());
		try {
			log.info("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		} catch (HttpClientErrorException e) {
			//log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			//log.error("Exception while fetching from searcher: ", e);
			throw new ServiceCallException(e.getMessage());
		}

		return response;
	}

	public Object fetchESResult(StringBuilder uri, Object request) {
		Object response = null;
		//log.debug("URI: " + uri.toString());
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", esAuthUtil.getESEncodedCredentials());
			final HttpEntity<Object> entity = new HttpEntity<>(request, headers);
			log.info("Request: " + mapper.writeValueAsString(request));
			log.info("Entity: " + mapper.writeValueAsString(entity));
			response = restTemplate.postForObject(uri.toString(), entity, Map.class);
		} catch (HttpClientErrorException e) {
			//log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			//log.error("Exception while fetching from searcher: ", e);
			throw new ServiceCallException(e.getMessage());
		}

		return response;
	}
	
	/**
	 * fetchResult form the different services based on the url and request object
	 * @param uri
	 * @param request
	 * @return
	 */
	public List fetchListResult(StringBuilder uri, Object request) {
		List response = null;
		//log.debug("URI: " + uri.toString());
		try {
			//log.debug("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, List.class);
		} catch (HttpClientErrorException e) {
			//log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			//log.error("Exception while fetching from searcher: ", e);
			throw new ServiceCallException(e.getMessage());
		}

		return response;
	}
	/**
	 * fetchResult form the different services based on the url and request object
	 * @param uri
	 * @param request
	 * @return
	 */
	public Integer fetchIntResult(StringBuilder uri, Object request) {
		Integer response = null;
		//log.debug("URI: " + uri.toString());
		try {
			//log.debug("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, Integer.class);
		} catch (HttpClientErrorException e) {
			//log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			//log.error("Exception while fetching from searcher: ", e);
			throw new ServiceCallException(e.getMessage());
		}

		return response;
	}

}
