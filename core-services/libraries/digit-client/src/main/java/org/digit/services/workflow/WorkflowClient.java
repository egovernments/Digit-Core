package org.digit.services.workflow;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.workflow.model.WorkflowProcessResponse;
import org.digit.services.workflow.model.WorkflowState;
import org.digit.services.workflow.model.WorkflowTransitionRequest;
import org.digit.services.workflow.model.WorkflowTransitionResponse;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class WorkflowClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public WorkflowClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public WorkflowTransitionResponse executeTransition(WorkflowTransitionRequest transitionRequest) {
        if (transitionRequest == null) {
            throw new DigitClientException("WorkflowTransitionRequest cannot be null");
        }
        if (transitionRequest.getProcessCode() == null || transitionRequest.getProcessCode().trim().isEmpty()) {
            throw new DigitClientException("Process code cannot be null or empty");
        }
        if (transitionRequest.getEntityId() == null || transitionRequest.getEntityId().trim().isEmpty()) {
            throw new DigitClientException("Entity ID cannot be null or empty");
        }
        if (transitionRequest.getAction() == null || transitionRequest.getAction().trim().isEmpty()) {
            throw new DigitClientException("Action cannot be null or empty");
        }
        try {
            log.debug("Executing workflow transition for processCode: {}, entityId: {}, action: {}", new Object[]{transitionRequest.getProcessCode(), transitionRequest.getEntityId(), transitionRequest.getAction()});
            String url = this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/transition";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity((Object)transitionRequest, headers), WorkflowTransitionResponse.class, new Object[0]);
            WorkflowTransitionResponse transitionResponse = (WorkflowTransitionResponse)response.getBody();
            log.debug("Successfully executed workflow transition. Response ID: {}", (Object)(transitionResponse != null ? transitionResponse.getId() : "null"));
            return transitionResponse;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to execute workflow transition: " + e.getMessage(), e);
        }
    }

    public WorkflowTransitionResponse executeTransition(String processCode, String entityId, String action, String comment) {
        WorkflowTransitionRequest request = WorkflowTransitionRequest.builder().processCode(processCode).entityId(entityId).action(action).comment(comment).build();
        return this.executeTransition(request);
    }

    public WorkflowTransitionResponse executeTransition(String processCode, String entityId, String action, String comment, Map<String, List<String>> attributes) {
        WorkflowTransitionRequest request = WorkflowTransitionRequest.builder().processCode(processCode).entityId(entityId).action(action).comment(comment).attributes(attributes).build();
        return this.executeTransition(request);
    }

    public List<WorkflowState> listStates(String processCode) {
        if (processCode == null || processCode.trim().isEmpty()) {
            throw new DigitClientException("Process code cannot be null or empty");
        }
        try {
            log.debug("Retrieving states for processCode: {}", (Object)processCode);
            String url = this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/process/" + processCode + "/state";
            ResponseEntity<List<WorkflowState>> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<WorkflowState>>(){}, new Object[0]);
            return response.getBody();
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to retrieve states: " + e.getMessage(), e);
        }
    }

    public WorkflowProcessResponse getProcessDefinition(String processCode) {
        if (processCode == null || processCode.trim().isEmpty()) {
            throw new DigitClientException("Process code cannot be null or empty");
        }
        try {
            log.debug("Retrieving workflow process definition with code: {}", (Object)processCode);
            String url = this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/process/definition/" + processCode;
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(new HttpHeaders()), WorkflowProcessResponse.class, new Object[0]);
            log.debug("Successfully retrieved workflow process definition: {}", (Object)processCode);
            return (WorkflowProcessResponse)response.getBody();
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to retrieve workflow process definition: " + e.getMessage(), e);
        }
    }
}
