package org.egov.handler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.repository.ServiceRequestRepository;
import org.egov.handler.web.models.BoundaryRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BoundaryUtil {

	private final ServiceConfiguration serviceConfig;

	private final ServiceRequestRepository serviceRequestRepository;

	@Autowired
	public BoundaryUtil(ServiceConfiguration serviceConfig, ServiceRequestRepository serviceRequestRepository) {
		this.serviceConfig = serviceConfig;
		this.serviceRequestRepository = serviceRequestRepository;
	}

	public void createBoundary(BoundaryRequest boundaryRequest) {
		StringBuilder uri = new StringBuilder(serviceConfig.getBoundaryCreateEndpoint());

		try {
			Object responseObject = serviceRequestRepository.fetchResult(uri, boundaryRequest);
		} catch (IllegalArgumentException e) {
			throw new CustomException("BOUNDARY_CREATION_FAILED", "Failed to create boundary for the tenant");
		}
	}
}
