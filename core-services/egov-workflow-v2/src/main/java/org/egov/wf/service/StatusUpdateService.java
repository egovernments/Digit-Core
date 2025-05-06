package org.egov.wf.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.util.BusinessUtil;
import org.egov.wf.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


@Service
public class StatusUpdateService {

	private Producer producer;

	private WorkflowConfig config;

	private BusinessUtil businessUtil;

	private WorkflowService workflowService;

	@Autowired
	public StatusUpdateService(Producer producer, WorkflowConfig config, BusinessUtil businessUtil, @Lazy WorkflowService workflowService) {
		this.producer = producer;
		this.config = config;
		this.businessUtil = businessUtil;
		this.workflowService = workflowService;
	}


	/**
	 * Updates the status and pushes the request on kafka to persist
	 *
	 * @param requestInfo
	 * @param processStateAndActions
	 */
	public void updateStatus(RequestInfo requestInfo, List<ProcessStateAndAction> processStateAndActions) {

		for (ProcessStateAndAction processStateAndAction : processStateAndActions) {
			if (processStateAndAction.getProcessInstanceFromRequest().getState() != null) {
				String prevStatus = processStateAndAction.getProcessInstanceFromRequest().getState().getUuid();
				processStateAndAction.getProcessInstanceFromRequest().setPreviousStatus(prevStatus);
			}
			processStateAndAction.getProcessInstanceFromRequest().setState(processStateAndAction.getResultantState());
			if (processStateAndAction.getResultantState().getTriggerParallelWorkflows() != null) {
				triggerParallelWorkflows(requestInfo, processStateAndAction, processStateAndAction.getResultantState().getTriggerParallelWorkflows());
			}
		}
		List<ProcessInstance> processInstances = new LinkedList<>();
		processStateAndActions.forEach(processStateAndAction -> {
			processInstances.add(processStateAndAction.getProcessInstanceFromRequest());
		});
		ProcessInstanceRequest processInstanceRequest = new ProcessInstanceRequest(requestInfo, processInstances);
		producer.push(processInstances.get(0).getTenantId(), config.getSaveTransitionTopic(), processInstanceRequest);
	}

	private void triggerParallelWorkflows(RequestInfo requestInfo, ProcessStateAndAction processStateAndAction, List<String> parallelWorkflows) {
		for (String parallelWorkflow : parallelWorkflows) {
			List<Action> actions = getParallelWorkflowAction(processStateAndAction.getProcessInstanceFromRequest().getTenantId(), parallelWorkflow);
			if (actions.isEmpty() || actions.get(0).getAction() == null) {
				throw new CustomException("INVALID ACTION", "Action not found in the " + parallelWorkflow
						+ " business service config for the businessId: " + processStateAndAction.getProcessInstanceFromRequest().getBusinessId());
			}
			triggerParallelWorkflow(requestInfo, processStateAndAction.getProcessInstanceFromRequest(), parallelWorkflow, actions.get(0).getAction());
		}
	}

	private void triggerParallelWorkflow(RequestInfo requestInfo, ProcessInstance processInstanceFromRequest, String parallelWorkflow, String action) {
		ProcessInstance processInstance = ProcessInstance.builder().businessService(parallelWorkflow)
				.businessId(processInstanceFromRequest.getBusinessId())
				.action(action)
				.moduleName(processInstanceFromRequest.getModuleName())
				.tenantId(processInstanceFromRequest.getTenantId())
				.build();
		List<ProcessInstance> processInstances = new LinkedList<>();
		processInstances.add(processInstance);
		ProcessInstanceRequest processInstanceRequest = new ProcessInstanceRequest(requestInfo, processInstances);
		System.out.println("businessservice:  " + parallelWorkflow);
		workflowService.transition(processInstanceRequest);
	}

	private List<Action> getParallelWorkflowAction(String tenantId, String businessServiceCode) {
		BusinessService businessService = businessUtil.getBusinessService(tenantId, businessServiceCode);
		for (State state : businessService.getStates()) {
			if (state.getState() == null) {
				return state.getActions();
			}
		}
		return Collections.emptyList();
	}

}
