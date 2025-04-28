package org.egov.handler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.repository.ServiceRequestRepository;
import org.egov.handler.web.models.IndividualRequest;
import org.egov.handler.web.models.IndividualResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IndividualUtil {

	private final ServiceConfiguration serviceConfig;

	private final ObjectMapper mapper;

	private final ServiceRequestRepository serviceRequestRepository;

	@Autowired
	public IndividualUtil(ServiceConfiguration serviceConfig, ObjectMapper mapper, ServiceRequestRepository serviceRequestRepository) {
		this.serviceConfig = serviceConfig;
		this.mapper = mapper;
		this.serviceRequestRepository = serviceRequestRepository;
	}

	public void createIndividual(IndividualRequest individualRequest) {
		StringBuilder uri = new StringBuilder(serviceConfig.getIndividualCreateEndpoint());

		try {
			Object responseObject = serviceRequestRepository.fetchResult(uri, individualRequest);
		} catch (IllegalArgumentException e) {
			throw new CustomException("INDIVIDUAL_CREATION_FAILED", "Failed to create individual for the tenant");
		}

	}
}
