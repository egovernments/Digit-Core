package org.egov.pgr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.repository.ServiceRequestRepository;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.pgr.web.models.ServiceWrapper;
import org.egov.pgr.web.models.Workflow;
import org.egov.pgr.web.models.camunda.CamundaProcessInstanceRequest;
import org.egov.pgr.web.models.camunda.CamundaTask;
import org.egov.pgr.web.models.camunda.Variable;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CamundaWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(CamundaWorkflowService.class);

    @Autowired
    private PGRConfiguration pgrConfiguration;

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Starts the Camunda workflow. This is the "push" part of the integration.
     * @param request The service request
     */
    public void startWorkflow(ServiceRequest request) {
        logger.info("Starting Camunda workflow for service request ID: {}", request.getService().getServiceRequestId());

        CamundaProcessInstanceRequest camundaRequest = createCamundaProcessInstanceRequest(request);

        // The process definition key should ideally be configurable.
        // For now, we are hardcoding it as "pgr-process". This must match the ID in the BPMN file.
        String processDefinitionKey = "pgr-process";

        StringBuilder url = new StringBuilder(pgrConfiguration.getCamundaHost());
        url.append(pgrConfiguration.getCamundaProcessDefinitionStartPath().replace("{key}", processDefinitionKey));

        try {
            Object response = repository.fetchResult(url, camundaRequest);
            logger.info("Camunda workflow start response: {}", response);
            // In a real scenario, you'd parse this response to get the process instance ID
            // and maybe update the application status based on the initial state in Camunda.
            // For now, we assume the initial status is set by the enrichment service.
        } catch (Exception e) {
            logger.error("Error occurred while starting Camunda workflow for service request ID: " + request.getService().getServiceRequestId(), e);
            throw new CustomException("CAMUNDA_WORKFLOW_START_ERROR", "Failed to start Camunda workflow.");
        }
    }

    /**
     * Updates the workflow in Camunda. This implements the two-step find and complete logic.
     * @param request The service request containing the update action.
     */
    public void updateWorkflow(ServiceRequest request) {
        String serviceRequestId = request.getService().getServiceRequestId();
        logger.info("Updating Camunda workflow for service request ID: {}", serviceRequestId);

        // Step 1: Find the active task for the given business key (serviceRequestId)
        CamundaTask activeTask = getTaskForBusinessKey(serviceRequestId);
        if (activeTask == null) {
            logger.error("No active task found in Camunda for service request ID: {}", serviceRequestId);
            throw new CustomException("CAMUNDA_TASK_NOT_FOUND", "No active Camunda task found for the given complaint.");
        }

        logger.info("Found active task '{}' (ID: {}) for service request ID: {}", activeTask.getName(), activeTask.getId(), serviceRequestId);

        // Step 2: Complete that specific task
        CamundaProcessInstanceRequest completionRequest = createCamundaTaskCompletionRequest(request);
        StringBuilder url = new StringBuilder(pgrConfiguration.getCamundaHost());
        url.append(pgrConfiguration.getCamundaTaskCompletePath().replace("{id}", activeTask.getId()));

        try {
            Object response = repository.fetchResult(url, completionRequest);
            logger.info("Camunda task completion response: {}", response);
        } catch (Exception e) {
            logger.error("Error occurred while completing Camunda task {} for service request ID: {}", activeTask.getId(), serviceRequestId, e);
            throw new CustomException("CAMUNDA_TASK_COMPLETION_ERROR", "Failed to complete Camunda task.");
        }
    }

    /**
     * Enriches the search results with workflow data from Camunda.
     * @param requestInfo The request info
     * @param serviceWrappers The list of service wrappers to enrich
     * @return The enriched list of service wrappers
     */
    public List<ServiceWrapper> enrichWorkflow(RequestInfo requestInfo, List<ServiceWrapper> serviceWrappers) {
        if (CollectionUtils.isEmpty(serviceWrappers)) {
            return serviceWrappers;
        }

        logger.info("Enriching workflow from Camunda for {} service requests.", serviceWrappers.size());

        for (ServiceWrapper wrapper : serviceWrappers) {
            String serviceRequestId = wrapper.getService().getServiceRequestId();
            CamundaTask task = getTaskForBusinessKey(serviceRequestId);

            if (task != null) {
                Workflow workflow = Workflow.builder()
                        .action(task.getName()) // The task name like "Assign Grievance" represents the current action/state
                        .assignes(task.getAssignee() != null ? Collections.singletonList(task.getAssignee()) : null)
                        .build();
                wrapper.setWorkflow(workflow);
            }
        }
        return serviceWrappers;
    }

    private CamundaTask getTaskForBusinessKey(String businessKey) {
        StringBuilder url = new StringBuilder(pgrConfiguration.getCamundaHost());
        url.append(pgrConfiguration.getCamundaTaskSearchPath());
        
        // Camunda's Task search API can be called via POST with a JSON body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("processInstanceBusinessKey", businessKey);


        try {
            Object response = repository.fetchResult(url, requestBody);
            logger.debug("Camunda task search response for businessKey {}: {}", businessKey, response);

            if (response == null) {
                logger.warn("Received null response from Camunda task search for businessKey: {}", businessKey);
                return null;
            }

            List<CamundaTask> tasks = mapper.convertValue(response, mapper.getTypeFactory().constructCollectionType(List.class, CamundaTask.class));

            if (CollectionUtils.isEmpty(tasks)) {
                logger.warn("No active tasks found in Camunda for businessKey: {}", businessKey);
                return null;
            }
            // A business key should have at most one active user task in this workflow
            return tasks.get(0);
        } catch (Exception e) {
            logger.error("Error during Camunda task search for businessKey: " + businessKey, e);
            throw new CustomException("CAMUNDA_TASK_SEARCH_ERROR", "Failed to search for Camunda task for business key: " + businessKey);
        }
    }

    private List<CamundaTask> getTasksForBusinessKeys(String businessKeys) {
        StringBuilder url = new StringBuilder(pgrConfiguration.getCamundaHost());
        url.append(pgrConfiguration.getCamundaTaskSearchPath());
        url.append("?processInstanceBusinessKeyIn=").append(businessKeys);

        try {
            Object response = repository.fetchResult(url, new HashMap<>());
            return mapper.convertValue(response, mapper.getTypeFactory().constructCollectionType(List.class, CamundaTask.class));
        } catch (Exception e) {
            throw new CustomException("CAMUNDA_BULK_TASK_SEARCH_ERROR", "Failed to bulk search for Camunda tasks.");
        }
    }

    private String getBusinessKeyForProcessInstance(String processInstanceId) {
        // This method is no longer needed with the simplified enrichment logic.
        return null;
    }


    /**
     * Creates the request body for starting a Camunda process instance.
     */
    private CamundaProcessInstanceRequest createCamundaProcessInstanceRequest(ServiceRequest serviceRequest) {
        Map<String, Variable> variables = extractVariables(serviceRequest);
        return new CamundaProcessInstanceRequest(variables, serviceRequest.getService().getServiceRequestId());
    }

    /**
     * Creates the request body for completing a Camunda task. This should only contain the workflow variables.
     */
    private CamundaProcessInstanceRequest createCamundaTaskCompletionRequest(ServiceRequest serviceRequest) {
        Map<String, Variable> variables = new HashMap<>();
        Workflow workflow = serviceRequest.getWorkflow();

        if (workflow != null) {
            variables.put("action", new Variable(workflow.getAction(), "String"));
            if (workflow.getComments() != null) {
                variables.put("comment", new Variable(workflow.getComments(), "String"));
            }
            if (!CollectionUtils.isEmpty(workflow.getAssignes())) {
                variables.put("assignee", new Variable(workflow.getAssignes().get(0), "String"));
            }
        }
        return CamundaProcessInstanceRequest.builder().variables(variables).build();
    }


    /**
     * Extracts all relevant variables from a ServiceRequest to be passed to Camunda when starting a process.
     */
    private Map<String, Variable> extractVariables(ServiceRequest serviceRequest) {
        Map<String, Variable> variables = new HashMap<>();

        // Add core service details
        if(serviceRequest.getService() != null){
            variables.put("serviceRequestId", new Variable(serviceRequest.getService().getServiceRequestId(), "String"));
            variables.put("tenantId", new Variable(serviceRequest.getService().getTenantId(), "String"));
            variables.put("serviceCode", new Variable(serviceRequest.getService().getServiceCode(), "String"));
            variables.put("source", new Variable(serviceRequest.getService().getSource(), "String"));
        }


        // Add workflow action details
        if (serviceRequest.getWorkflow() != null) {
            variables.put("action", new Variable(serviceRequest.getWorkflow().getAction(), "String"));
            if (serviceRequest.getWorkflow().getComments() != null) {
                variables.put("comment", new Variable(serviceRequest.getWorkflow().getComments(), "String"));
            }

            // Add assignee if provided
            if (!CollectionUtils.isEmpty(serviceRequest.getWorkflow().getAssignes())) {
                variables.put("assignee", new Variable(serviceRequest.getWorkflow().getAssignes().get(0), "String"));
            }
        }

        // Add citizen info
        if (serviceRequest.getRequestInfo() != null && serviceRequest.getRequestInfo().getUserInfo() != null) {
            variables.put("citizenName", new Variable(serviceRequest.getRequestInfo().getUserInfo().getName(), "String"));
            variables.put("citizenMobileNumber", new Variable(serviceRequest.getRequestInfo().getUserInfo().getMobileNumber(), "String"));
            variables.put("citizenUuid", new Variable(serviceRequest.getRequestInfo().getUserInfo().getUuid(), "String"));
        }

        return variables;
    }
} 