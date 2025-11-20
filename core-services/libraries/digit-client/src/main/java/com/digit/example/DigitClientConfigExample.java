package com.digit.example;

import com.digit.factory.DigitClientFactory;
import com.digit.services.account.AccountClient;
import com.digit.services.boundary.BoundaryClient;
import com.digit.services.workflow.WorkflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Smart configuration class that automatically uses application.properties if available,
 * otherwise falls back to default values (http://localhost:8080).
 * 
 * Optional properties for application.properties:
 * digit.services.account.base-url=http://your-account-service:8080
 * digit.services.boundary.base-url=http://your-boundary-service:8080
 * digit.services.workflow.base-url=http://your-workflow-service:8085
 *
 * NOTE: This is an EXAMPLE configuration class. Remove @Configuration if you want to
 * use your own configuration or if this conflicts with your application configuration.
 */
//@Configuration  // <-- COMMENTED OUT to avoid bean definition conflicts
public class DigitClientConfigExample {

    /**
     * Creates a BoundaryClient bean that automatically reads from application.properties if available,
     * otherwise uses default localhost:8080.
     * 
     * @param baseUrl the boundary service base URL (uses default if property not found)
     * @return configured BoundaryClient ready for autowiring
     */
    @Bean
    public BoundaryClient boundaryClient(@Value("${digit.services.boundary.base-url:http://localhost:8080}") String baseUrl) {
        return DigitClientFactory.createBoundaryClient(baseUrl);
    }

    /**
     * Creates an AccountClient bean that automatically reads from application.properties if available,
     * otherwise uses default localhost:8080.
     * 
     * @param baseUrl the account service base URL (uses default if property not found)
     * @return configured AccountClient ready for autowiring
     */
    @Bean
    public AccountClient accountClient(@Value("${digit.services.account.base-url:http://localhost:8080}") String baseUrl) {
        return DigitClientFactory.createAccountClient(baseUrl);
    }

    /**
     * Creates a WorkflowClient bean that automatically reads from application.properties if available,
     * otherwise uses default localhost:8085.
     * 
     * @param baseUrl the workflow service base URL (uses default if property not found)
     * @return configured WorkflowClient ready for autowiring
     */
    @Bean
    public WorkflowClient workflowClient(@Value("${digit.services.workflow.base-url:http://localhost:8085}") String baseUrl) {
        return DigitClientFactory.createWorkflowClient(baseUrl);
    }
}
