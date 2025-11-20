package com.digit.factory;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientErrorHandler;
import com.digit.services.account.AccountClient;
import com.digit.services.boundary.BoundaryClient;
import com.digit.services.workflow.WorkflowClient;
import com.digit.services.idgen.IdGenClient;
import com.digit.services.notification.NotificationClient;
import com.digit.services.individual.IndividualClient;
import com.digit.services.filestore.FilestoreClient;
import com.digit.services.mdms.MdmsClient;
import org.springframework.web.client.RestTemplate;

/**
 * Factory class for creating pre-configured Digit service clients.
 * Handles all internal configuration including RestTemplate setup, error handling, and base URLs.
 */
public class DigitClientFactory {

    /**
     * Creates a pre-configured BoundaryClient with default base URL (http://localhost:8080).
     *
     * @return configured BoundaryClient ready for use
     */
    public static BoundaryClient createBoundaryClient() {
        return createBoundaryClient("http://localhost:8080");
    }

    /**
     * Creates a pre-configured BoundaryClient with the specified base URL.
     *
     * @param baseUrl the base URL for the Boundary service
     * @return configured BoundaryClient ready for use
     */
    public static BoundaryClient createBoundaryClient(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Boundary service base URL cannot be null or empty");
        }

        // Create and configure RestTemplate
        RestTemplate restTemplate = createRestTemplate();
        
        // Create ApiProperties with the provided base URL
        ApiProperties apiProperties = createApiProperties(null, baseUrl, null, null);
        
        return new BoundaryClient(restTemplate, apiProperties);
    }

    /**
     * Creates a pre-configured AccountClient with default base URL (http://localhost:8080).
     *
     * @return configured AccountClient ready for use
     */
    public static AccountClient createAccountClient() {
        return createAccountClient("http://localhost:8080");
    }

    /**
     * Creates a pre-configured AccountClient with the specified base URL.
     *
     * @param baseUrl the base URL for the Account service
     * @return configured AccountClient ready for use
     */
    public static AccountClient createAccountClient(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Account service base URL cannot be null or empty");
        }

        // Create and configure RestTemplate
        RestTemplate restTemplate = createRestTemplate();
        
        // Create ApiProperties with the provided base URL
        ApiProperties apiProperties = createApiProperties(baseUrl, null, null, null);
        
        return new AccountClient(restTemplate, apiProperties);
    }

    /**
     * Creates a pre-configured WorkflowClient with default base URL (http://localhost:8085).
     *
     * @return configured WorkflowClient ready for use
     */
    public static WorkflowClient createWorkflowClient() {
        return createWorkflowClient("http://localhost:8080");
    }

    /**
     * Creates a pre-configured WorkflowClient with the specified base URL.
     *
     * @param baseUrl the base URL for the Workflow service
     * @return configured WorkflowClient ready for use
     */
    public static WorkflowClient createWorkflowClient(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow service base URL cannot be null or empty");
        }

        // Create and configure RestTemplate
        RestTemplate restTemplate = createRestTemplate();
        
        // Create ApiProperties with the provided base URL
        ApiProperties apiProperties = createApiProperties(null, null, baseUrl, null);
        
        return new WorkflowClient(restTemplate, apiProperties);
    }

    /**
     * Creates a pre-configured IdGenClient with default base URL (http://localhost:8100).
     *
     * @return configured IdGenClient instance
     */
    public static IdGenClient createIdGenClient() {
        return createIdGenClient("http://localhost:8100");
    }

    /**
     * Creates a pre-configured IdGenClient with custom base URL.
     *
     * @param baseUrl the base URL for the IdGen service
     * @return configured IdGenClient instance
     */
    public static IdGenClient createIdGenClient(String baseUrl) {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setIdgenServiceUrl(baseUrl);
        
        RestTemplate restTemplate = createRestTemplate();
        
        return new IdGenClient(restTemplate, apiProperties);
    }

    /**
     * Creates a pre-configured NotificationClient with default base URL (http://localhost:8091).
     *
     * @return configured NotificationClient instance
     */
    public static NotificationClient createNotificationClient() {
        return createNotificationClient("http://localhost:8091");
    }

    /**
     * Creates a pre-configured NotificationClient with custom base URL.
     *
     * @param baseUrl the base URL for the Notification service
     * @return configured NotificationClient instance
     */
    public static NotificationClient createNotificationClient(String baseUrl) {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setNotificationServiceUrl(baseUrl);
        
        RestTemplate restTemplate = createRestTemplate();
        return new NotificationClient(restTemplate, apiProperties);
    }

    /**
     * Creates a pre-configured IndividualClient with default base URL (http://localhost:8999).
     *
     * @return configured IndividualClient instance
     */
    public static IndividualClient createIndividualClient() {
        return createIndividualClient("http://localhost:8999");
    }

    /**
     * Creates a pre-configured IndividualClient with custom base URL.
     *
     * @param baseUrl the base URL for the Individual service
     * @return configured IndividualClient instance
     */
    public static IndividualClient createIndividualClient(String baseUrl) {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setIndividualServiceUrl(baseUrl);
        
        RestTemplate restTemplate = createRestTemplate();
        return new IndividualClient(restTemplate, apiProperties);
    }

    /**
     * Creates a pre-configured FilestoreClient with default base URL (http://localhost:8080).
     *
     * @return configured FilestoreClient instance
     */
    public static FilestoreClient createFilestoreClient() {
        return createFilestoreClient("http://localhost:8080");
    }

    /**
     * Creates a pre-configured FilestoreClient with custom base URL.
     *
     * @param baseUrl the base URL for the Filestore service
     * @return configured FilestoreClient instance
     */
    public static FilestoreClient createFilestoreClient(String baseUrl) {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setFilestoreServiceUrl(baseUrl);
        
        RestTemplate restTemplate = createRestTemplate();
        return new FilestoreClient(restTemplate, apiProperties);
    }

    /**
     * Creates a pre-configured MdmsClient with default base URL (http://localhost:8080).
     *
     * @return configured MdmsClient instance
     */
    public static MdmsClient createMdmsClient() {
        return createMdmsClient("http://localhost:8080");
    }

    /**
     * Creates a pre-configured MdmsClient with custom base URL.
     *
     * @param baseUrl the base URL for the MDMS service
     * @return configured MdmsClient instance
     */
    public static MdmsClient createMdmsClient(String baseUrl) {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setMdmsServiceUrl(baseUrl);
        
        RestTemplate restTemplate = createRestTemplate();
        return new MdmsClient(restTemplate, apiProperties);
    }

    /**
     * Creates both AccountClient and BoundaryClient with default base URLs (http://localhost:8080).
     *
     * @return DigitClients container with both clients
     */
    public static DigitClients createClients() {
        return createClients("http://localhost:8080", "http://localhost:8080");
    }

    /**
     * Creates both AccountClient and BoundaryClient with their respective base URLs.
     *
     * @param accountBaseUrl the base URL for the Account service
     * @param boundaryBaseUrl the base URL for the Boundary service
     * @return DigitClients container with both clients
     */
    public static DigitClients createClients(String accountBaseUrl, String boundaryBaseUrl) {
        return createClients(accountBaseUrl, boundaryBaseUrl, "http://localhost:8085");
    }

    /**
     * Creates all clients (Account, Boundary, Workflow, IdGen) with their respective base URLs.
     *
     * @param accountBaseUrl the base URL for the Account service
     * @param boundaryBaseUrl the base URL for the Boundary service
     * @param workflowBaseUrl the base URL for the Workflow service
     * @param idgenBaseUrl the base URL for the IdGen service
     * @return DigitClients container with all clients
     */
    public static DigitClients createClients(String accountBaseUrl, String boundaryBaseUrl, String workflowBaseUrl, String idgenBaseUrl) {
        AccountClient accountClient = createAccountClient(accountBaseUrl);
        BoundaryClient boundaryClient = createBoundaryClient(boundaryBaseUrl);
        WorkflowClient workflowClient = createWorkflowClient(workflowBaseUrl);
        IdGenClient idGenClient = createIdGenClient(idgenBaseUrl);
        
        return new DigitClients(accountClient, boundaryClient, workflowClient, idGenClient);
    }

    /**
     * Creates all three clients (Account, Boundary, Workflow) with their respective base URLs.
     *
     * @param accountBaseUrl the base URL for the Account service
     * @param boundaryBaseUrl the base URL for the Boundary service
     * @param workflowBaseUrl the base URL for the Workflow service
     * @return DigitClients container with all clients
     */
    public static DigitClients createClients(String accountBaseUrl, String boundaryBaseUrl, String workflowBaseUrl) {
        return createClients(accountBaseUrl, boundaryBaseUrl, workflowBaseUrl, "http://localhost:8100");
    }

    /**
     * Creates a configured RestTemplate with error handling.
     */
    private static RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DigitClientErrorHandler());
        return restTemplate;
    }

    /**
     * Creates ApiProperties with the specified base URLs.
     */
    private static ApiProperties createApiProperties(String accountBaseUrl, String boundaryBaseUrl, String workflowBaseUrl, String idgenBaseUrl) {
        ApiProperties apiProperties = new ApiProperties();
        
        if (accountBaseUrl != null) {
            // Use reflection or direct field access since we can't use @Value here
            setField(apiProperties, "accountServiceUrl", accountBaseUrl);
        }
        
        if (boundaryBaseUrl != null) {
            setField(apiProperties, "boundaryServiceUrl", boundaryBaseUrl);
        }
        
        if (workflowBaseUrl != null) {
            setField(apiProperties, "workflowServiceUrl", workflowBaseUrl);
        }
        
        if (idgenBaseUrl != null) {
            setField(apiProperties, "idgenServiceUrl", idgenBaseUrl);
        }
        
        // Set default MDMS service URL
        setField(apiProperties, "mdmsServiceUrl", "http://localhost:8080");
        
        // Set default values for other properties
        setField(apiProperties, "connectTimeout", 5000);
        setField(apiProperties, "readTimeout", 30000);
        setField(apiProperties, "maxRetryAttempts", 3);
        setField(apiProperties, "retryDelay", 1000L);
        
        return apiProperties;
    }

    /**
     * Helper method to set field values using reflection.
     */
    private static void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    /**
     * Container class for multiple Digit service clients.
     */
    public static class DigitClients {
        private final AccountClient accountClient;
        private final BoundaryClient boundaryClient;
        private final WorkflowClient workflowClient;
        private final IdGenClient idGenClient;
        private final IndividualClient individualClient;
        private final MdmsClient mdmsClient;

        public DigitClients(AccountClient accountClient, BoundaryClient boundaryClient) {
            this.accountClient = accountClient;
            this.boundaryClient = boundaryClient;
            this.workflowClient = null;
            this.idGenClient = null;
            this.individualClient = null;
            this.mdmsClient = null;
        }

        public DigitClients(AccountClient accountClient, BoundaryClient boundaryClient, WorkflowClient workflowClient) {
            this.accountClient = accountClient;
            this.boundaryClient = boundaryClient;
            this.workflowClient = workflowClient;
            this.idGenClient = null;
            this.individualClient = null;
            this.mdmsClient = null;
        }

        public DigitClients(AccountClient accountClient, BoundaryClient boundaryClient, WorkflowClient workflowClient, IdGenClient idGenClient) {
            this.accountClient = accountClient;
            this.boundaryClient = boundaryClient;
            this.workflowClient = workflowClient;
            this.idGenClient = idGenClient;
            this.individualClient = null;
            this.mdmsClient = null;
        }

        public DigitClients(AccountClient accountClient, BoundaryClient boundaryClient, WorkflowClient workflowClient, IdGenClient idGenClient, IndividualClient individualClient) {
            this.accountClient = accountClient;
            this.boundaryClient = boundaryClient;
            this.workflowClient = workflowClient;
            this.idGenClient = idGenClient;
            this.individualClient = individualClient;
            this.mdmsClient = null;
        }

        public DigitClients(AccountClient accountClient, BoundaryClient boundaryClient, WorkflowClient workflowClient, IdGenClient idGenClient, IndividualClient individualClient, MdmsClient mdmsClient) {
            this.accountClient = accountClient;
            this.boundaryClient = boundaryClient;
            this.workflowClient = workflowClient;
            this.idGenClient = idGenClient;
            this.individualClient = individualClient;
            this.mdmsClient = mdmsClient;
        }

        public AccountClient getAccountClient() {
            return accountClient;
        }

        public BoundaryClient getBoundaryClient() {
            return boundaryClient;
        }

        public WorkflowClient getWorkflowClient() {
            return workflowClient;
        }

        public IdGenClient getIdGenClient() {
            return idGenClient;
        }

        public IndividualClient getIndividualClient() {
            return individualClient;
        }

        public MdmsClient getMdmsClient() {
            return mdmsClient;
        }
    }
}
