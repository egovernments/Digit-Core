package org.egov.autocomplete_workflows.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.autocomplete_workflows.config.ServiceConfiguration;
import org.egov.autocomplete_workflows.models.*;
import org.egov.autocomplete_workflows.util.WorkflowUtil;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AutocompleteService {

	private final WorkflowUtil workflowUtil;

	private final ServiceConfiguration serviceConfig;

	public AutocompleteService(WorkflowUtil workflowUtil, ServiceConfiguration serviceConfig) {
		this.workflowUtil = workflowUtil;
		this.serviceConfig = serviceConfig;
	}

	public void terminateParallelWorkflows(RequestInfo requestInfo, ProcessInstance processInstance) {
		try {
			BusinessServiceResponse businessServiceResponse = workflowUtil.getBusinessService(requestInfo, processInstance.getTenantId(), processInstance.getBusinessService());
			List<String> parallelWorkflowList = extractUniqueParallelWorkflows(businessServiceResponse.getBusinessServices().get(0));

			for (String parallelWorkflow : parallelWorkflowList) {
				ProcessInstanceResponse processInstanceResponse = workflowUtil.searchProcessInstance(requestInfo, processInstance.getTenantId(), processInstance.getBusinessId(), false, parallelWorkflow);
				if (processInstanceResponse.getProcessInstances().isEmpty()) continue;

				Boolean isTerminateState = processInstanceResponse.getProcessInstances().get(0).getState().getIsTerminateState();

				if (Boolean.FALSE.equals(isTerminateState)) {
					ProcessInstance parallelWfProcessInstance = new ProcessInstance();
					parallelWfProcessInstance.setTenantId(processInstance.getTenantId());
					parallelWfProcessInstance.setBusinessService(parallelWorkflow);
					parallelWfProcessInstance.setBusinessId(processInstance.getBusinessId());
					parallelWfProcessInstance.setAction(serviceConfig.getAutocompleteActionMap().get(processInstance.getAction()));

					ProcessInstanceRequest processInstanceRequest = new ProcessInstanceRequest();
					processInstanceRequest.setRequestInfo(requestInfo);
					processInstanceRequest.setProcessInstances(Collections.singletonList(parallelWfProcessInstance));

					workflowUtil.transitionProcessInstance(processInstanceRequest);
				}
			}
		} catch (Exception e) {
			log.error("Failed to terminate parallel workflows for businessId {}: {}", processInstance.getBusinessId(), e.getMessage(), e);
		}
	}

	private List<String> extractUniqueParallelWorkflows(BusinessService businessService) {
		if (businessService == null || businessService.getStates() == null) {
			return Collections.emptyList();
		}

		return businessService.getStates().stream()
				.filter(state -> state.getTriggerParallelWorkflows() != null)
				.flatMap(state -> state.getTriggerParallelWorkflows().stream())
				.distinct()
				.collect(Collectors.toList());
	}
}
