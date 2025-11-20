package com.digit.services.workflow;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientException;
import com.digit.services.workflow.model.WorkflowTransitionRequest;
import com.digit.services.workflow.model.WorkflowTransitionResponse;
import com.digit.services.workflow.model.WorkflowProcessResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Map;

/**
 * Service client for Workflow API operations.
 * Provides methods to interact with the Workflow service.
 */
@Slf4j
@Getter
@Setter
public class WorkflowClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for WorkflowClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public WorkflowClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;

        // DEBUG: Check which RestTemplate we're using
        System.out.println("🔍 WorkflowClient created with RestTemplate: " + restTemplate.getClass().getSimpleName());
        System.out.println("🔍 RestTemplate interceptors: " + restTemplate.getInterceptors().size());
        restTemplate.getInterceptors().forEach(interceptor -> {
            System.out.println("  - " + interceptor.getClass().getSimpleName());
        });
    }

    /**
     * Executes a workflow transition.
     *
     * @param transitionRequest the workflow transition request
     * @return the WorkflowTransitionResponse object
     * @throws DigitClientException if transition fails
     */
    public WorkflowTransitionResponse executeTransition(WorkflowTransitionRequest transitionRequest) {
        if (transitionRequest == null) {
            throw new DigitClientException("WorkflowTransitionRequest cannot be null");
        }

        if (transitionRequest.getProcessId() == null || transitionRequest.getProcessId().trim().isEmpty()) {
            throw new DigitClientException("Process ID cannot be null or empty");
        }

        if (transitionRequest.getEntityId() == null || transitionRequest.getEntityId().trim().isEmpty()) {
            throw new DigitClientException("Entity ID cannot be null or empty");
        }

        if (transitionRequest.getAction() == null || transitionRequest.getAction().trim().isEmpty()) {
            throw new DigitClientException("Action cannot be null or empty");
        }

        try {
            log.debug("Executing workflow transition for processId: {}, entityId: {}, action: {}", 
                     transitionRequest.getProcessId(), transitionRequest.getEntityId(), transitionRequest.getAction());
            
            String url = apiProperties.getWorkflowServiceUrl() + "/workflow/v1/transition";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<WorkflowTransitionRequest> entity = new HttpEntity<>(transitionRequest, headers);
            
            ResponseEntity<WorkflowTransitionResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, WorkflowTransitionResponse.class);
            
            WorkflowTransitionResponse transitionResponse = response.getBody();
            log.debug("Successfully executed workflow transition. Response ID: {}", 
                     transitionResponse != null ? transitionResponse.getId() : "null");
            
            return transitionResponse;
            
        } catch (Exception e) {
            log.error("Failed to execute workflow transition for processId: {}, entityId: {}", 
                     transitionRequest.getProcessId(), transitionRequest.getEntityId(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to execute workflow transition: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a workflow transition with simplified parameters.
     *
     * @param processId the process ID
     * @param entityId the entity ID
     * @param action the action to perform
     * @param comment optional comment
     * @return the WorkflowTransitionResponse object
     * @throws DigitClientException if transition fails
     */
    public WorkflowTransitionResponse executeTransition(String processId, String entityId, String action, String comment) {
        WorkflowTransitionRequest request = WorkflowTransitionRequest.builder()
                .processId(processId)
                .entityId(entityId)
                .action(action)
                .comment(comment)
                .build();
        
        return executeTransition(request);
    }

    /**
     * Executes a workflow transition with attributes.
     *
     * @param processId the process ID
     * @param entityId the entity ID
     * @param action the action to perform
     * @param comment optional comment
     * @param attributes the workflow attributes
     * @return the WorkflowTransitionResponse object
     * @throws DigitClientException if transition fails
     */
    public WorkflowTransitionResponse executeTransition(String processId, String entityId, String action, 
                                                       String comment, Map<String, List<String>> attributes) {
        WorkflowTransitionRequest request = WorkflowTransitionRequest.builder()
                .processId(processId)
                .entityId(entityId)
                .action(action)
                .comment(comment)
                .attributes(attributes)
                .build();
        
        return executeTransition(request);
    }

    /**
     * Retrieves a workflow process by its ID.
     *
     * @param processId the process ID to retrieve
     * @return the WorkflowProcessResponse object containing process details
     * @throws DigitClientException if retrieval fails
     */
    public WorkflowProcessResponse getProcessById(String processId) {
        if (processId == null || processId.trim().isEmpty()) {
            throw new DigitClientException("Process ID cannot be null or empty");
        }

        try {
            log.debug("Retrieving workflow process with ID: {}", processId);
            
            String url = apiProperties.getWorkflowServiceUrl() + "/workflow/v1/process/" + processId;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<WorkflowProcessResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, WorkflowProcessResponse.class);
            
            WorkflowProcessResponse processResponse = response.getBody();
            log.debug("Successfully retrieved workflow process: {}", processId);
            
            return processResponse;
            
        } catch (Exception e) {
            log.error("Failed to retrieve workflow process with ID: {}", processId, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to retrieve workflow process: " + e.getMessage(), e);
        }
    }

public String getProcessByCode(String code) {
    if (code == null || code.trim().isEmpty()) {
        throw new DigitClientException("Process code cannot be null or empty");
    }

    try {
        log.debug("Retrieving workflow process with code: {}", code);

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(apiProperties.getWorkflowServiceUrl())
                  .append("/workflow/v1/process")
                  .append("?code=")
                  .append(URLEncoder.encode(code, StandardCharsets.UTF_8));

        String url = urlBuilder.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<WorkflowProcessResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<WorkflowProcessResponse>>() {}
        );

        List<WorkflowProcessResponse> processes = response.getBody();

        if (processes == null || processes.isEmpty()) {
            log.warn("No workflow process found for code: {}", code);
            throw new DigitClientException("No workflow process found for code: " + code);
        }

        String processId = processes.get(0).getId();
        log.debug("Successfully retrieved workflow process ID: {}", processId);
        return processId;

    } catch (Exception e) {
        log.error("Failed to retrieve workflow process for code: {}", code, e);
        throw new DigitClientException("Failed to retrieve workflow process: " + e.getMessage(), e);
    }
}



}
