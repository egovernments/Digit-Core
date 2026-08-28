package org.digit.example;

import org.digit.factory.DigitClientFactory;
import org.digit.services.boundary.BoundaryClient;
import org.digit.services.workflow.WorkflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class DigitClientConfigExample {
    @Bean
    public BoundaryClient boundaryClient(@Value(value="${digit.services.boundary.base-url:http://localhost:8080}") String baseUrl) {
        return DigitClientFactory.createBoundaryClient(baseUrl);
    }

    @Bean
    public WorkflowClient workflowClient(@Value(value="${digit.services.workflow.base-url:http://localhost:8085}") String baseUrl) {
        return DigitClientFactory.createWorkflowClient(baseUrl);
    }
}

