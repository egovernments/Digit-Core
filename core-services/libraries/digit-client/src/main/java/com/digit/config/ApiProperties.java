package com.digit.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Digit service API endpoints.
 * Holds configurable base URLs for different services.
 */
@Getter
@Setter
public class ApiProperties {

    @Value("${digit.services.account.base-url:http://localhost:8080}")
    private String accountServiceUrl;

    @Value("${digit.services.boundary.base-url:http://localhost:8080}")
    private String boundaryServiceUrl;

    @Value("${digit.services.workflow.base-url:http://localhost:8085}")
    private String workflowServiceUrl;

    @Value("${digit.services.idgen.base-url:http://localhost:8100}")
    private String idgenServiceUrl;

    @Value("${digit.services.notification.base-url:http://localhost:8091}")
    private String notificationServiceUrl;

    @Value("${digit.services.individual.base-url:http://localhost:8999}")
    private String individualServiceUrl;

    @Value("${digit.services.filestore.base-url:http://localhost:8080}")
    private String filestoreServiceUrl;

    @Value("${digit.services.mdms.base-url:http://localhost:8080}")
    private String mdmsServiceUrl;

    @Value("${digit.services.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${digit.services.timeout.read:30000}")
    private int readTimeout;

    @Value("${digit.services.retry.max-attempts:3}")
    private int maxRetryAttempts;

    // Explicit getter methods as fallback for Lombok
    public String getAccountServiceUrl() {
        return accountServiceUrl;
    }

    public String getBoundaryServiceUrl() {
        return boundaryServiceUrl;
    }

    public String getWorkflowServiceUrl() {
        return workflowServiceUrl;
    }

    public String getIdgenServiceUrl() {
        return idgenServiceUrl;
    }

    public String getIndividualServiceUrl() {
        return individualServiceUrl;
    }

    public String getFilestoreServiceUrl() {
        return filestoreServiceUrl;
    }

    public String getMdmsServiceUrl() {
        return mdmsServiceUrl;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    @Value("${digit.services.retry.delay:1000}")
    private long retryDelay;

    public long getRetryDelay() {
        return retryDelay;
    }
}
