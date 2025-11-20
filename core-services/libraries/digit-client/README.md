# Digit Client Library

A reusable Java Spring client library for Digit services. This library provides a modular, extensible, and developer-friendly way to interact with Digit microservices.

## Features

- **Modular Design**: Separate clients for different services (Account, Boundary, Workflow, Individual, Filestore)
- **Common Models**: Shared models like `AuditDetails` for consistent data structures
- **Spring Framework 6+**: Uses modern Spring features with Java 17
- **Error Handling**: Comprehensive error handling with custom exceptions
- **Configurable**: Easy configuration through properties
- **Extensible**: Simple to add new service clients
- **Production Ready**: Includes timeouts, retry logic, and proper logging
- **OpenTelemetry Support**: Built-in distributed tracing
- **Header Propagation**: Automatic propagation of headers like X-Correlation-ID

## Project Structure

```
digit-client/
├── src/main/java/com/digit/
│   ├── config/
│   │   ├── ApiConfig.java              # Spring configuration with RestTemplate bean
│   │   └── ApiProperties.java          # Configurable service URLs and settings
│   ├── exception/
│   │   ├── DigitClientException.java       # Custom exception class
│   │   └── DigitClientErrorHandler.java    # HTTP error handler
│   ├── common/
│   │   └── model/
│   │       └── AuditDetails.java       # Common audit fields for all entities
│   ├── services/
│   │   ├── account/                    # Account service client
│   │   │   ├── model/
│   │   │   │   ├── Tenant.java         # Tenant POJO
│   │   │   │   └── TenantConfig.java   # Tenant config POJO
│   │   │   └── AccountClient.java      # Account service client
│   │   ├── boundary/                   # Boundary service client
│   │   │   ├── model/
│   │   │   │   ├── Boundary.java           # Boundary POJO
│   │   │   │   ├── BoundarySearchResponse.java
│   │   │   │   ├── BoundaryRelationship.java
│   │   │   │   └── BoundaryHierarchy.java
│   │   │   └── BoundaryClient.java     # Boundary service client
│   │   ├── workflow/                   # Workflow service client
│   │   │   ├── model/
│   │   │   │   ├── Workflow.java           # Workflow process definition
│   │   │   │   ├── Document.java           # Document model
│   │   │   │   ├── WorkflowProcessResponse.java
│   │   │   │   └── WorkflowTransitionResponse.java
│   │   │   └── WorkflowClient.java     # Workflow service client
│   │   ├── individual/                 # Individual service client
│   │   │   ├── model/
│   │   │   │   ├── Individual.java
│   │   │   │   └── AdditionalFields.java
│   │   │   └── IndividualClient.java
│   │   └── filestore/                  # Filestore service client
│   │       └── FilestoreClient.java
│   └── example/
│       └── DigitClientExample.java     # Usage examples
└── pom.xml
```

## Dependencies

- Spring Framework 6.1.0
- Java 17
- Lombok 1.18.30
- Jackson 2.15.3

## Configuration

Configure service URLs and timeouts using system properties or environment variables:

```properties
# Service URLs
digit.services.account.base-url=http://localhost:8080/account
digit.services.boundary.base-url=http://localhost:8080/boundary
digit.services.workflow.base-url=http://localhost:8080/workflow
digit.services.individual.base-url=http://localhost:8080/individual
digit.services.filestore.base-url=http://localhost:8080/filestore

# Timeout settings
digit.services.timeout.connect=5000
digit.services.timeout.read=30000

# Retry settings
digit.services.retry.max-attempts=3
digit.services.retry.delay=1000

# OpenTelemetry Configuration
otel.service.name=your-service-name
otel.exporter.otlp.endpoint=http://localhost:4317
otel.traces.exporter=otlp
otel.traces.sampler.ratio=1.0

# Header Propagation
digit.propagate.headers.allow=authorization,x-correlation-id,x-request-id,x-tenant-id,x-user-id
digit.propagate.headers.prefixes=x-ctx-,x-trace-
```

## Usage

### Basic Setup

```java
import com.digit.config.ApiConfig;
import com.digit.services.account.AccountClient;
import com.digit.services.boundary.BoundaryClient;
import com.digit.services.workflow.WorkflowClient;
import com.digit.services.individual.IndividualClient;
import com.digit.services.filestore.FilestoreClient;

// Initialize Spring context
ApplicationContext context = new AnnotationConfigApplicationContext(ApiConfig.class);

// Get client beans
AccountClient accountClient = context.getBean(AccountClient.class);
BoundaryClient boundaryClient = context.getBean(BoundaryClient.class);
WorkflowClient workflowClient = context.getBean(WorkflowClient.class);
IndividualClient individualClient = context.getBean(IndividualClient.class);
FilestoreClient filestoreClient = context.getBean(FilestoreClient.class);
```

### Account Operations

```java
// Get tenant by ID
Tenant tenant = accountClient.getTenantById("1234");

// Get all tenants
List<Tenant> tenants = accountClient.getAllTenants();

// Get tenant by code
Tenant tenantByCode = accountClient.getTenantByCode("default");

// Create new tenant
Tenant newTenant = Tenant.builder()
    .code("test-tenant")
    .name("Test Tenant")
    .email("test@example.com")
    .build();
Tenant createdTenant = accountClient.createTenant(newTenant);
```

### Workflow Operations

```java
// Get workflow process by code
String processId = workflowClient.getProcessByCode("process-code");

// Execute a simple workflow transition
WorkflowTransitionResponse response = workflowClient.executeTransition(
    "process-123",      // processId
    "entity-456",       // entityId
    "APPROVE",          // action
    "Approving request"  // comment
);

// Execute a workflow transition with attributes
Map<String, List<String>> attributes = new HashMap<>();
attributes.put("approverRole", List.of("MANAGER"));
attributes.put("priority", List.of("HIGH"));

WorkflowTransitionResponse responseWithAttrs = workflowClient.executeTransition(
    "process-123",
    "entity-456",
    "APPROVE",
    "Approving with attributes",
    attributes
);

// Get workflow process details by ID
WorkflowProcessResponse process = workflowClient.getProcessById("process-123");
```

### Individual Operations

```java
// Create a new individual
Individual newIndividual = Individual.builder()
    .givenName("John")
    .familyName("Doe")
    .gender("MALE")
    .dateOfBirth("1990-01-01")
    .build();
Individual created = individualClient.createIndividual(newIndividual);

// Get individual by ID
Individual individual = individualClient.getIndividualById("ind-123");

// Search individuals by name (with pagination)
IndividualSearchResponse searchResponse = individualClient.searchIndividuals("John", 10, 0);
List<Individual> individuals = searchResponse.getIndividuals();

// Search all individuals (with pagination)
IndividualSearchResponse allIndividuals = individualClient.searchAllIndividuals(20, 0);

// Check if individual exists by ID
boolean individualExists = individualClient.searchIndividualByID("ind-123");
if (individualExists) {
    // Individual exists, you can fetch the full details if needed
    Individual individual = individualClient.getIndividualById("ind-123");
}
```

### Filestore Operations

```java
// Check if file exists by ID and tenant ID
boolean fileExists = filestoreClient.getFile("file-123", "tenant-123");

// Check if file exists with validation (throws DigitClientException for client errors)
boolean fileExistsWithValidation = filestoreClient.getFileWithValidation("file-123", "tenant-123");
```

### Error Handling

The library provides comprehensive error handling:

```java
try {
    Account account = accountClient.getAccountById("1234");
} catch (DigitClientException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("HTTP Status: " + e.getHttpStatus());
    System.err.println("Error Code: " + e.getErrorCode());
}
```

## Building the Project

```bash
mvn clean compile
```

## Running Examples

```bash
mvn exec:java -Dexec.mainClass="com.digit.example.DigitClientExample"
```

## Adding New Service Clients

To add a new service client:

1. Create a new package under `com.digit.services`
2. Add model classes in the `model` subpackage
3. Create a client class following the pattern of existing clients
4. Add service URL configuration to `ApiProperties`
5. The client will be automatically discovered by Spring's component scanning

## Common Models

### AuditDetails

A common model used across all entities for audit trail:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditDetails {
    private String createdBy;
    private String lastModifiedBy;
    private Long createdTime;
    private Long lastModifiedTime;
}
```

### Document

Used for workflow document attachments:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {
    private String id;
    private String documentType;
    private String fileStoreId;
    private String documentUid;
    private AuditDetails auditDetails;
}
```

### Workflow

Workflow process definition:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Workflow {
    private String action;
    private List<String> assignes;
    private String comments;
    private List<Document> verificationDocuments;
    private AuditDetails auditDetails;
}
```

### Model Annotations

All model classes use Lombok annotations for clean code:

- `@Data`: Generates getters, setters, toString, equals, and hashCode
- `@Builder`: Provides builder pattern
- `@NoArgsConstructor` / `@AllArgsConstructor`: Generates constructors
- `@JsonIgnoreProperties(ignoreUnknown = true)`: Ignores unknown JSON properties

## Header Propagation

The library automatically propagates common headers like `X-Correlation-ID` and `X-Tenant-ID` between services. This is handled by the `HeaderPropagationInterceptor` which is automatically configured.

### Custom Headers

To add custom headers to be propagated, update the configuration:

```properties
digit.propagate.headers.allow=authorization,x-custom-header,x-request-id
digit.propagate.headers.prefixes=x-ctx-,x-trace-,x-myapp-
```

## OpenTelemetry Integration

The library includes built-in OpenTelemetry support for distributed tracing. To enable:

1. Add the following dependencies to your project:
   ```xml
   <dependency>
       <groupId>io.opentelemetry</groupId>
       <artifactId>opentelemetry-api</artifactId>
   </dependency>
   <dependency>
       <groupId>io.opentelemetry</groupId>
       <artifactId>opentelemetry-sdk</artifactId>
   </dependency>
   ```

2. Configure OpenTelemetry in your `application.properties`:
   ```properties
   otel.service.name=your-service-name
   otel.exporter.otlp.endpoint=http://localhost:4317
   otel.traces.exporter=otlp
   otel.traces.sampler.ratio=1.0
   ```

## Next Steps

1. Review the API documentation for each service client
2. Add authentication/authorization if required
3. Implement additional service clients as needed
4. Add unit tests for the clients
5. Configure logging and monitoring as per your requirements

The library is now ready to use and can be easily extended with additional Digit services!
