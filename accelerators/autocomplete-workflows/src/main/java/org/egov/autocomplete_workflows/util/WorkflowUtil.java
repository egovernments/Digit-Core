package org.egov.autocomplete_workflows.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.autocomplete_workflows.config.ServiceConfiguration;
import org.egov.autocomplete_workflows.models.BusinessServiceResponse;
import org.egov.autocomplete_workflows.models.ProcessInstanceRequest;
import org.egov.autocomplete_workflows.models.ProcessInstanceResponse;
import org.egov.autocomplete_workflows.repository.ServiceRequestRepository;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

@Component
public class WorkflowUtil {

	private final ServiceConfiguration serviceConfiguration;

	private final ServiceRequestRepository serviceRequestRepository;

	private final ObjectMapper mapper;

	public WorkflowUtil(ServiceConfiguration serviceConfiguration, ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper) {
		this.serviceConfiguration = serviceConfiguration;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
	}

	public BusinessServiceResponse getBusinessService(RequestInfo requestInfo, String tenantId, String businessServiceName) {
		StringBuilder uri = new StringBuilder(serviceConfiguration.getWfBusinessServiceSearchURI());
		uri.append("?tenantId=").append(tenantId);
		uri.append("&businessServices=").append(businessServiceName);

		RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
		requestInfoWrapper.setRequestInfo(requestInfo);

		return mapper.convertValue(serviceRequestRepository.fetchResult(uri, requestInfoWrapper), BusinessServiceResponse.class);
	}

	public void transitionProcessInstance(ProcessInstanceRequest processInstanceRequest) {
		StringBuilder uri = new StringBuilder(serviceConfiguration.getWfProcessTransitionURI());
		serviceRequestRepository.fetchResult(uri, processInstanceRequest);
	}

	public ProcessInstanceResponse searchProcessInstance(RequestInfo requestInfo, String tenantId, String businessIds, Boolean history, String businessService) {

		StringBuilder uri = new StringBuilder(serviceConfiguration.getWfProcessSearchURI());
		uri.append("?tenantId=").append(tenantId);
		uri.append("&businessIds=").append(businessIds);
		uri.append("&history=").append(history);
		uri.append("&businessService=").append(businessService);

		RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
		requestInfoWrapper.setRequestInfo(requestInfo);

		return mapper.convertValue(serviceRequestRepository.fetchResult(uri, requestInfoWrapper), ProcessInstanceResponse.class);
	}
}
