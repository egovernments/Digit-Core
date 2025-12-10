# Digit Client Library

A comprehensive Java Spring client library for Digit services. This library provides a modular, extensible, and developer-friendly way to interact with Digit microservices with proper function naming, clear arguments, and comprehensive documentation.

## Features

- **8 Service Clients**: Account, Boundary, Workflow, Individual, Filestore, IdGen, MDMS, Notification
- **Type-Safe APIs**: Strongly typed request/response models with proper validation
- **Spring Framework 6+**: Modern Spring features with Java 17 support
- **Comprehensive Error Handling**: Custom exceptions with detailed error information
- **Production Ready**: Built-in timeouts, retry logic, and structured logging
- **OpenTelemetry Integration**: Distributed tracing support out of the box
- **Header Propagation**: Automatic propagation of correlation and tenant headers
- **Flexible Configuration**: Easy setup through properties or environment variables

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
digit.services.idgen.base-url=http://localhost:8080/idgen
digit.services.mdms.base-url=http://localhost:8080/mdms
digit.services.notification.base-url=http://localhost:8080/notification

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

## Quick Start

### 1. Add Dependency

Add the digit-client library to your project dependencies.

### 2. Basic Setup

#### Option A: Minimal Configuration (Recommended)

Create a simple configuration class in your project:

```java
package com.yourproject.config;

import com.digit.config.ApiConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Minimal configuration class for Digit Client Library integration.
 * Everything else is auto-configured by HeaderPropagationAutoConfiguration.
 */
@Configuration
@Import(ApiConfig.class)  // Import digit-client configuration (RestTemplate with interceptor)
public class DigitClientConfig {
    // That's it! Everything else auto-configured:
    // - ApiProperties (from application.properties)
    // - BoundaryClient (with RestTemplate + ApiProperties)
    // - AccountClient (with RestTemplate + ApiProperties)
    // - WorkflowClient (with RestTemplate + ApiProperties)
    // - IndividualClient (with RestTemplate + ApiProperties)
    // - FilestoreClient (with RestTemplate + ApiProperties)
    // - IdGenClient (with RestTemplate + ApiProperties)
    // - MdmsClient (with RestTemplate + ApiProperties)
    // - NotificationClient (with RestTemplate + ApiProperties)
    // - HeaderPropagationInterceptor (automatic header propagation)
}
```

Then inject clients directly into your services:

```java
@Service
public class YourService {
    
    private final AccountClient accountClient;
    private final BoundaryClient boundaryClient;
    private final WorkflowClient workflowClient;
    // ... other clients as needed
    
    public YourService(AccountClient accountClient, 
                      BoundaryClient boundaryClient,
                      WorkflowClient workflowClient) {
        this.accountClient = accountClient;
        this.boundaryClient = boundaryClient;
        this.workflowClient = workflowClient;
    }
    
    // Use the clients in your business logic
}
```

#### Option B: Manual Context Setup

```java
import com.digit.config.ApiConfig;
import com.digit.services.account.AccountClient;
import com.digit.services.boundary.BoundaryClient;
import com.digit.services.workflow.WorkflowClient;
import com.digit.services.individual.IndividualClient;
import com.digit.services.filestore.FilestoreClient;
import com.digit.services.idgen.IdGenClient;
import com.digit.services.mdms.MdmsClient;
import com.digit.services.notification.NotificationClient;

// Initialize Spring context with configuration
ApplicationContext context = new AnnotationConfigApplicationContext(ApiConfig.class);

// Get client beans for all 8 services
AccountClient accountClient = context.getBean(AccountClient.class);
BoundaryClient boundaryClient = context.getBean(BoundaryClient.class);
WorkflowClient workflowClient = context.getBean(WorkflowClient.class);
IndividualClient individualClient = context.getBean(IndividualClient.class);
FilestoreClient filestoreClient = context.getBean(FilestoreClient.class);
IdGenClient idGenClient = context.getBean(IdGenClient.class);
MdmsClient mdmsClient = context.getBean(MdmsClient.class);
NotificationClient notificationClient = context.getBean(NotificationClient.class);
```

### 3. Configure Service URLs

Set up your service endpoints in `application.properties`:

```properties
# Required: Service base URLs
digit.services.account.base-url=http://localhost:8080/account
digit.services.boundary.base-url=http://localhost:8080/boundary
digit.services.workflow.base-url=http://localhost:8080/workflow
digit.services.individual.base-url=http://localhost:8080/individual
digit.services.filestore.base-url=http://localhost:8080/filestore
digit.services.idgen.base-url=http://localhost:8080/idgen
digit.services.mdms.base-url=http://localhost:8080/mdms
digit.services.notification.base-url=http://localhost:8080/notification
```

## Service Client APIs

### 1. Account Service Client

The AccountClient manages tenant and tenant configuration operations.

#### Usage Examples

```java
// Create a new tenant
Tenant newTenant = Tenant.builder()
    .code("test-tenant")
    .name("Test Tenant")
    .emailId("test@example.com")
    .build();
Tenant createdTenant = accountClient.createTenant(newTenant);

// Search tenant by code
Tenant tenant = accountClient.searchTenantByCode("default");

// Update existing tenant
Tenant updatedTenant = accountClient.updateTenant("tenant-123", tenant);

// Create tenant configuration
TenantConfig config = TenantConfig.builder()
    .name("Config Name")
    .code("config-code")
    .value("config-value")
    .build();
TenantConfig createdConfig = accountClient.createTenantConfig(config);

// Search tenant config by code
TenantConfig tenantConfig = accountClient.searchTenantConfigByCode("config-code");

// Update tenant configuration
TenantConfig updatedConfig = accountClient.updateTenantConfig("config-123", config);
```

#### Account Service Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `createTenant(Tenant tenant)` | `tenant` - Tenant object to create | `Tenant` - Created tenant | Creates a new tenant record |
| `searchTenantByCode(String code)` | `code` - Tenant code to search | `Tenant` - Found tenant or null | Searches for tenant by unique code |
| `updateTenant(String tenantId, Tenant tenant)` | `tenantId` - Tenant ID, `tenant` - Updated data | `Tenant` - Updated tenant | Updates existing tenant |
| `createTenantConfig(TenantConfig config)` | `config` - Configuration to create | `TenantConfig` - Created config | Creates tenant configuration |
| `searchTenantConfigByCode(String code)` | `code` - Config code to search | `TenantConfig` - Found config or null | Searches config by code |
| `updateTenantConfig(String configId, TenantConfig config)` | `configId` - Config ID, `config` - Updated data | `TenantConfig` - Updated config | Updates tenant configuration |

**All methods throw `DigitClientException` on validation errors or API failures.**

### 2. Boundary Service Client

The BoundaryClient manages geographical boundaries, hierarchies, and relationships.

#### Usage Examples

```java
// Create boundaries
List<Boundary> boundaries = List.of(
    Boundary.builder()
        .code("BOUNDARY_001")
        .name("Test Boundary")
        .type("ADMINISTRATIVE")
        .build()
);
List<Boundary> createdBoundaries = boundaryClient.createBoundaries(boundaries);

// Search boundaries by codes
List<String> codes = List.of("BOUNDARY_001", "BOUNDARY_002");
List<Boundary> foundBoundaries = boundaryClient.searchBoundariesByCodes(codes);

// Validate boundary codes exist
boolean allValid = boundaryClient.isValidBoundariesByCodes(codes);

// Update boundary
Boundary updatedBoundary = boundaryClient.updateBoundary("boundary-123", boundary);

// Create boundary hierarchy
BoundaryHierarchy hierarchy = BoundaryHierarchy.builder()
    .hierarchyType("ADMIN")
    .boundaryType("DISTRICT")
    .parentBoundaryType("STATE")
    .build();
BoundaryHierarchy createdHierarchy = boundaryClient.createBoundaryHierarchy(hierarchy);

// Search boundary hierarchy
BoundaryHierarchy foundHierarchy = boundaryClient.searchBoundaryHierarchy("ADMIN");

// Create boundary relationship
BoundaryRelationship relationship = BoundaryRelationship.builder()
    .code("REL_001")
    .hierarchyType("ADMIN")
    .boundary("boundary-id")
    .parent("parent-boundary-id")
    .build();
BoundaryRelationship createdRelationship = boundaryClient.createBoundaryRelationship(relationship);

// Search boundary relationships with hierarchy
List<BoundarySearchResponse.HierarchyRelation> relationships = 
    boundaryClient.searchBoundaryRelationships("ADMIN", "DISTRICT", true);

// Update boundary relationship
BoundaryRelationship updatedRelationship = 
    boundaryClient.updateBoundaryRelationship("rel-123", relationship);
```

#### Boundary Service Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `createBoundaries(List<Boundary> boundaries)` | `boundaries` - List of boundaries to create | `List<Boundary>` - Created boundaries | Creates multiple boundary records |
| `searchBoundariesByCodes(List<String> codes)` | `codes` - Boundary codes to search | `List<Boundary>` - Found boundaries | Searches boundaries by codes |
| `isValidBoundariesByCodes(List<String> codes)` | `codes` - Boundary codes to validate | `boolean` - True if all valid | Validates boundary codes exist |
| `updateBoundary(String boundaryId, Boundary boundary)` | `boundaryId` - Boundary ID, `boundary` - Updated data | `Boundary` - Updated boundary | Updates existing boundary |
| `createBoundaryHierarchy(BoundaryHierarchy hierarchy)` | `hierarchy` - Hierarchy definition | `BoundaryHierarchy` - Created hierarchy | Creates boundary hierarchy |
| `searchBoundaryHierarchy(String hierarchyType)` | `hierarchyType` - Hierarchy type | `BoundaryHierarchy` - Found hierarchy | Searches hierarchy by type |
| `createBoundaryRelationship(BoundaryRelationship relationship)` | `relationship` - Relationship to create | `BoundaryRelationship` - Created relationship | Creates boundary relationship |
| `searchBoundaryRelationships(String hierarchyType, String boundaryType, boolean includeChildren)` | `hierarchyType` - Hierarchy type, `boundaryType` - Boundary type (optional), `includeChildren` - Include children flag | `List<HierarchyRelation>` - Relationships | Searches relationships with hierarchy |
| `updateBoundaryRelationship(String relationshipId, BoundaryRelationship relationship)` | `relationshipId` - Relationship ID, `relationship` - Updated data | `BoundaryRelationship` - Updated relationship | Updates boundary relationship |

**All methods throw `DigitClientException` on validation errors or API failures.**

### 3. Workflow Service Client

The WorkflowClient manages workflow processes and state transitions.

#### Usage Examples

```java
// Get workflow process ID by code
String processId = workflowClient.getProcessByCode("CITIZEN_REGISTRATION");

// Execute a simple workflow transition
WorkflowTransitionResponse response = workflowClient.executeTransition(
    "process-123",      // processId
    "entity-456",       // entityId
    "APPROVE",          // action
    "Approving request" // comment
);

// Execute workflow transition with attributes
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

// Execute transition with full request object
WorkflowTransitionRequest request = WorkflowTransitionRequest.builder()
    .processId("process-123")
    .entityId("entity-456")
    .action("APPROVE")
    .comment("Custom comment")
    .attributes(attributes)
    .build();
WorkflowTransitionResponse fullResponse = workflowClient.executeTransition(request);

// Get workflow process details by ID
WorkflowProcessResponse process = workflowClient.getProcessById("process-123");
```

#### Workflow Service Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `executeTransition(WorkflowTransitionRequest request)` | `request` - Complete transition request | `WorkflowTransitionResponse` - Transition result | Executes workflow transition with full request |
| `executeTransition(String processId, String entityId, String action, String comment)` | `processId` - Process ID, `entityId` - Entity ID, `action` - Action to perform, `comment` - Optional comment | `WorkflowTransitionResponse` - Transition result | Executes simple workflow transition |
| `executeTransition(String processId, String entityId, String action, String comment, Map<String, List<String>> attributes)` | `processId` - Process ID, `entityId` - Entity ID, `action` - Action, `comment` - Comment, `attributes` - Workflow attributes | `WorkflowTransitionResponse` - Transition result | Executes transition with attributes |
| `getProcessById(String processId)` | `processId` - Process ID to retrieve | `WorkflowProcessResponse` - Process details | Retrieves workflow process by ID |
| `getProcessByCode(String code)` | `code` - Process code to search | `String` - Process ID | Gets process ID by code |

**Required Fields**: For transitions - `processId`, `entityId`, and `action` cannot be null or empty.
**All methods throw `DigitClientException` on validation errors or API failures.**

### 4. Individual Service Client

The IndividualClient manages individual citizen records with comprehensive search and pagination support.

#### Usage Examples

```java
// Create a new individual
Individual newIndividual = Individual.builder()
    .givenName("John")
    .familyName("Doe")
    .gender("MALE")
    .dateOfBirth("1990-01-01")
    .mobileNumber("9876543210")
    .email("john.doe@example.com")
    .build();
Individual created = individualClient.createIndividual(newIndividual);

// Get individual by ID
Individual individual = individualClient.getIndividualById("ind-123");

// Search individuals by name with default pagination (limit=10, offset=0)
IndividualSearchResponse searchResponse = individualClient.searchIndividualsByName("John");

// Search individuals by name with custom pagination
IndividualSearchResponse searchResponsePaginated = individualClient.searchIndividualsByName("John", 20, 10);

// Search all individuals with default pagination
IndividualSearchResponse allIndividuals = individualClient.searchAllIndividuals();

// Search all individuals with custom pagination
IndividualSearchResponse allIndividualsPaginated = individualClient.searchAllIndividuals(50, 0);

// Check if individual exists by ID (with default pagination)
boolean individualExists = individualClient.isIndividualExist("ind-123");

// Check if individual exists by ID with custom pagination
boolean individualExistsCustom = individualClient.isIndividualExistsById("ind-123", 10, 0);

if (individualExists) {
    // Individual exists, fetch full details
    Individual individual = individualClient.getIndividualById("ind-123");
}
```

#### Individual Service Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `createIndividual(Individual individual)` | `individual` - Individual object to create | `Individual` - Created individual | Creates new individual record |
| `getIndividualById(String individualId)` | `individualId` - Individual ID to retrieve | `Individual` - Individual object | Retrieves individual by unique ID |
| `searchIndividualsByName(String individualName)` | `individualName` - Name to search (optional) | `IndividualSearchResponse` - Search results | Searches by name with default pagination (10, 0) |
| `searchIndividualsByName(String individualName, Integer limit, Integer offset)` | `individualName` - Name to search, `limit` - Max results, `offset` - Skip count | `IndividualSearchResponse` - Search results | Searches by name with custom pagination |
| `searchAllIndividuals()` | None | `IndividualSearchResponse` - All individuals | Retrieves all individuals with default pagination |
| `searchAllIndividuals(Integer limit, Integer offset)` | `limit` - Max results, `offset` - Skip count | `IndividualSearchResponse` - All individuals | Retrieves all individuals with custom pagination |
| `isIndividualExist(String individualId)` | `individualId` - ID to check | `boolean` - Exists flag | Checks existence with default pagination |
| `isIndividualExistsById(String individualId, Integer limit, Integer offset)` | `individualId` - ID to check, `limit` - Max results, `offset` - Skip count | `boolean` - Exists flag | Checks existence with custom pagination |

**Pagination Defaults**: limit=10, offset=0 when not specified or invalid values provided.
**All methods throw `DigitClientException` on validation errors or API failures.**

#### Individual Model Structure

The Individual model contains the following key fields:

```java
Individual individual = Individual.builder()
    // Basic Information
    .individualId("unique-id")           // Auto-generated unique identifier
    .clientReferenceId("client-ref")     // Client-provided reference
    .givenName("John")                   // First name
    .familyName("Doe")                   // Last name
    .otherNames("Middle")                // Additional names
    .dateOfBirth("1990-01-01")          // Date of birth (YYYY-MM-DD format)
    .gender("MALE")                      // Gender (MALE/FEMALE/OTHER)
    .age(33)                            // Age in years
    .bloodGroup("O+")                   // Blood group
    
    // Contact Information
    .mobileNumber("9876543210")         // Primary mobile number
    .mobileNumberVerified(true)         // Mobile verification status
    .altContactNumber("9876543211")     // Alternative contact number
    .email("john@example.com")          // Email address
    .emailVerified(true)                // Email verification status
    
    // Personal Details
    .fatherName("Father Name")          // Father's name
    .husbandName("Husband Name")        // Husband's name (if applicable)
    .relationship("SELF")               // Relationship type
    .photo("photo-url")                 // Photo URL or file reference
    
    // System Fields
    .userId("user-123")                 // Associated user ID
    .userUuid("user-uuid")              // User UUID
    .username("johndoe")                // Username
    .type("CITIZEN")                    // Individual type
    .active(true)                       // Active status
    .isSystemUser(false)                // System user flag
    
    // Additional Data
    .additionalDetails(Map.of("key", "value"))  // Custom additional details
    .address(List.of(address))          // List of Address objects
    .identifiers(List.of(identifier))   // List of Identifier objects (ID proofs)
    .skills(List.of(skill))             // List of Skill objects
    .additionalFields(additionalFields) // Additional custom fields
    .auditDetails(auditDetails)         // Audit information
    .build();
```

#### Account Service Models

**Tenant Model:**
```java
Tenant tenant = Tenant.builder()
    .id("tenant-123")                   // Auto-generated unique identifier
    .code("TENANT_CODE")                // Unique tenant code
    .name("Tenant Name")                // Display name
    .description("Description")         // Optional description
    .logoId("logo-file-id")            // Logo file reference
    .imageId("image-file-id")          // Image file reference
    .domainUrl("https://tenant.com")    // Tenant domain URL
    .type("CITY")                       // Tenant type
    .address("Address")                 // Physical address
    .city("City Name")                  // City
    .pincode("123456")                  // PIN code
    .contactNumber("1234567890")        // Contact number
    .helpLineNumber("1800-123-456")     // Help line number
    .emailId("contact@tenant.com")      // Email address
    .OfficeTimings(officeTimings)       // Office timings object
    .twitterUrl("https://twitter.com")  // Social media URLs
    .facebookUrl("https://facebook.com")
    .auditDetails(auditDetails)         // Audit information
    .build();
```

**TenantConfig Model:**
```java
TenantConfig config = TenantConfig.builder()
    .id("config-123")                   // Auto-generated unique identifier
    .code("CONFIG_CODE")                // Unique config code
    .name("Config Name")                // Display name
    .description("Description")         // Optional description
    .value("config-value")              // Configuration value
    .auditDetails(auditDetails)         // Audit information
    .build();
```

#### Boundary Service Models

**Boundary Model:**
```java
Boundary boundary = Boundary.builder()
    .id("boundary-123")                 // Auto-generated unique identifier
    .code("BOUNDARY_CODE")              // Unique boundary code
    .name("Boundary Name")              // Display name
    .type("ADMINISTRATIVE")             // Boundary type (ADMINISTRATIVE, REVENUE, etc.)
    .parent("parent-boundary-id")       // Parent boundary ID
    .children(List.of("child-id-1"))    // List of child boundary IDs
    .materializedPath("ROOT.PARENT.CHILD") // Hierarchical path
    .auditDetails(auditDetails)         // Audit information
    .build();
```

**BoundaryHierarchy Model:**
```java
BoundaryHierarchy hierarchy = BoundaryHierarchy.builder()
    .id("hierarchy-123")                // Auto-generated unique identifier
    .hierarchyType("ADMIN")             // Type of hierarchy
    .boundaryType("DISTRICT")           // Boundary type in this hierarchy
    .parentBoundaryType("STATE")        // Parent boundary type
    .active(true)                       // Active status
    .auditDetails(auditDetails)         // Audit information
    .build();
```

**BoundaryRelationship Model:**
```java
BoundaryRelationship relationship = BoundaryRelationship.builder()
    .id("relationship-123")             // Auto-generated unique identifier
    .code("REL_CODE")                   // Unique relationship code
    .tenantId("tenant-123")             // Tenant ID
    .hierarchyType("ADMIN")             // Hierarchy type
    .boundary("boundary-id")            // Boundary ID
    .parent("parent-boundary-id")       // Parent boundary ID
    .auditDetails(auditDetails)         // Audit information
    .build();
```

#### Workflow Service Models

**WorkflowTransitionRequest Model:**
```java
WorkflowTransitionRequest request = WorkflowTransitionRequest.builder()
    .processId("process-123")           // Workflow process ID (required)
    .entityId("entity-456")             // Entity being processed (required)
    .action("APPROVE")                  // Action to perform (required)
    .comment("Approval comment")        // Optional comment
    .attributes(Map.of(                 // Optional workflow attributes
        "approverRole", List.of("MANAGER"),
        "priority", List.of("HIGH")
    ))
    .build();
```

**WorkflowTransitionResponse Model:**
```java
// Response contains:
response.getId()                        // Transition ID
response.getProcessId()                 // Process ID
response.getEntityId()                  // Entity ID
response.getAction()                    // Action performed
response.getStatus()                    // Transition status
response.getComment()                   // Comment
response.getAttributes()                // Attributes map
response.getAuditDetails()              // Audit information
```

**WorkflowProcessResponse Model:**
```java
// Response contains:
process.getId()                         // Process ID
process.getCode()                       // Process code
process.getName()                       // Process name
process.getDescription()                // Process description
process.getActive()                     // Active status
process.getAuditDetails()               // Audit information
```

### Filestore Service Operations

```java
// Check if file is available (returns false for any error)
boolean fileExists = filestoreClient.isFileAvailable("file-123", "tenant-123");

// Validate file availability (throws exception for client errors)
boolean fileExistsWithValidation = filestoreClient.validateFileAvailability("file-123", "tenant-123");
```

#### Filestore Service Functions

**1. isFileAvailable(String fileId, String tenantId)**
- **Purpose**: Checks if a file exists and is available
- **Parameters**: 
  - `fileId` (String) - The ID of the file to check (cannot be null or empty)
  - `tenantId` (String) - The tenant ID (cannot be null or empty)
- **Returns**: boolean - true if file exists (200 OK), false for any error
- **Throws**: DigitClientException only for parameter validation errors
- **Note**: Returns false for both client and server errors without throwing exceptions

**2. validateFileAvailability(String fileId, String tenantId)**
- **Purpose**: Checks file availability with enhanced error handling
- **Parameters**: 
  - `fileId` (String) - The ID of the file to check (cannot be null or empty)
  - `tenantId` (String) - The tenant ID (cannot be null or empty)
- **Returns**: boolean - true if file exists (200 OK), false for server errors
- **Throws**: DigitClientException for client errors (4xx status codes) and parameter validation
- **Note**: Distinguishes between client errors (throws exception) and server errors (returns false)

### IdGen Service Operations

```java
// Generate ID with full request object
IdGenGenerateRequest request = IdGenGenerateRequest.builder()
    .templateCode("TEMPLATE_CODE")
    .variables(Map.of("key1", "value1", "key2", "value2"))
    .build();
String generatedId = idGenClient.generateId(request);

// Generate ID with template code and variables
Map<String, String> variables = Map.of("city", "BANGALORE", "year", "2024");
String idWithVars = idGenClient.generateId("CITIZEN_ID", variables);

// Generate ID with just template code
String simpleId = idGenClient.generateId("SIMPLE_TEMPLATE");
```

#### IdGen Service Functions

**1. generateId(IdGenGenerateRequest generateRequest)**
- **Purpose**: Generates an ID using the specified template and variables
- **Parameters**: `generateRequest` (IdGenGenerateRequest) - The complete ID generation request (cannot be null)
- **Required Fields**: templateCode (cannot be null or empty)
- **Returns**: String - The generated ID
- **Throws**: DigitClientException if generation fails, request is null, or generated ID is empty

**2. generateId(String templateCode, Map<String, String> variables)**
- **Purpose**: Generates an ID with simplified parameters including variables
- **Parameters**: 
  - `templateCode` (String) - The template code (cannot be null or empty)
  - `variables` (Map<String, String>) - Variables for template substitution (can be null)
- **Returns**: String - The generated ID
- **Throws**: DigitClientException if generation fails

**3. generateId(String templateCode)**
- **Purpose**: Generates an ID with just template code (no variables)
- **Parameters**: `templateCode` (String) - The template code (cannot be null or empty)
- **Returns**: String - The generated ID
- **Throws**: DigitClientException if generation fails

### MDMS Service Operations

```java
// Validate MDMS data (tenant ID from X-Tenant-ID header)
Set<String> identifiers = Set.of("IDENTIFIER_1", "IDENTIFIER_2", "IDENTIFIER_3");
boolean allValid = mdmsClient.isMdmsDataValid("SCHEMA_CODE", identifiers);

// Search MDMS data
List<Mdms> mdmsData = mdmsClient.searchMdmsData("SCHEMA_CODE", identifiers);
```

#### MDMS Service Functions

**1. isMdmsDataValid(String schemaCode, Set<String> uniqueIdentifiers)**
- **Purpose**: Validates whether all provided unique identifiers exist for the given schema code
- **Parameters**: 
  - `schemaCode` (String) - The schema code to validate against (cannot be null or empty)
  - `uniqueIdentifiers` (Set<String>) - Set of unique identifiers to validate (cannot be null or empty)
- **Returns**: boolean - true if all unique identifiers are valid (found), false otherwise
- **Throws**: DigitClientException if request fails or input is invalid
- **Note**: Tenant ID is automatically propagated via X-Tenant-ID header

**2. searchMdmsData(String schemaCode, Set<String> uniqueIdentifiers)**
- **Purpose**: Searches MDMS data by schema code and unique identifiers
- **Parameters**: 
  - `schemaCode` (String) - The schema code (cannot be null or empty)
  - `uniqueIdentifiers` (Set<String>) - Set of unique identifiers to search for (cannot be null or empty)
- **Returns**: List<Mdms> - The list of found MDMS objects
- **Throws**: DigitClientException if search fails
- **Note**: Tenant ID is automatically propagated via X-Tenant-ID header

### Notification Service Operations

```java
// Send email with full request object
SendEmailRequest emailRequest = SendEmailRequest.builder()
    .templateId("EMAIL_TEMPLATE_ID")
    .version("1.0")
    .emailIds(List.of("user1@example.com", "user2@example.com"))
    .payload(Map.of("name", "John", "amount", "1000"))
    .enrich(false)
    .build();
SendEmailResponse emailResponse = notificationClient.sendEmail(emailRequest);

// Send email with simplified parameters
Map<String, Object> payload = Map.of("customerName", "John Doe", "orderId", "ORD123");
SendEmailResponse simpleEmailResponse = notificationClient.sendEmail(
    "ORDER_CONFIRMATION", "1.0", List.of("customer@example.com"), payload);

// Send SMS with full request object
SendSMSRequest smsRequest = SendSMSRequest.builder()
    .templateId("SMS_TEMPLATE_ID")
    .version("1.0")
    .mobileNumbers(List.of("9876543210", "9876543211"))
    .payload(Map.of("otp", "123456", "name", "John"))
    .category(SendSMSRequest.SMSCategory.TRANSACTIONAL)
    .enrich(false)
    .build();
SendSMSResponse smsResponse = notificationClient.sendSMS(smsRequest);

// Send SMS with simplified parameters
SendSMSResponse simpleSmsResponse = notificationClient.sendSMS(
    "OTP_TEMPLATE", "1.0", List.of("9876543210"), 
    Map.of("otp", "123456"), SendSMSRequest.SMSCategory.TRANSACTIONAL);
```

#### Notification Service Functions

**1. sendEmail(SendEmailRequest emailRequest)**
- **Purpose**: Sends an email notification using the specified template and parameters
- **Parameters**: `emailRequest` (SendEmailRequest) - The complete email send request (cannot be null)
- **Required Fields**: templateId, emailIds (cannot be null or empty)
- **Returns**: SendEmailResponse - The email send response with status
- **Throws**: DigitClientException if email sending fails or required fields are missing

**2. sendEmail(String templateId, String version, List<String> emailIds, Map<String, Object> payload)**
- **Purpose**: Sends an email with simplified parameters
- **Parameters**: 
  - `templateId` (String) - The template ID (cannot be null or empty)
  - `version` (String) - The template version (can be null)
  - `emailIds` (List<String>) - List of email addresses (cannot be null or empty)
  - `payload` (Map<String, Object>) - Payload data for template substitution (can be null)
- **Returns**: SendEmailResponse - The email send response
- **Throws**: DigitClientException if email sending fails

**3. sendSMS(SendSMSRequest smsRequest)**
- **Purpose**: Sends an SMS notification using the specified template and parameters
- **Parameters**: `smsRequest` (SendSMSRequest) - The complete SMS send request (cannot be null)
- **Required Fields**: templateId, mobileNumbers (cannot be null or empty)
- **Returns**: SendSMSResponse - The SMS send response with status
- **Throws**: DigitClientException if SMS sending fails or required fields are missing

**4. sendSMS(String templateId, String version, List<String> mobileNumbers, Map<String, Object> payload, SendSMSRequest.SMSCategory category)**
- **Purpose**: Sends an SMS with simplified parameters
- **Parameters**: 
  - `templateId` (String) - The template ID (cannot be null or empty)
  - `version` (String) - The template version (can be null)
  - `mobileNumbers` (List<String>) - List of mobile numbers (cannot be null or empty)
  - `payload` (Map<String, Object>) - Payload data for template substitution (can be null)
  - `category` (SMSCategory) - SMS category (TRANSACTIONAL, PROMOTIONAL, etc.)
- **Returns**: SendSMSResponse - The SMS send response
- **Throws**: DigitClientException if SMS sending fails

#### Additional Service Models

**IdGenGenerateRequest Model:**
```java
IdGenGenerateRequest request = IdGenGenerateRequest.builder()
    .templateCode("TEMPLATE_CODE")        // Template code (required)
    .variables(Map.of(                    // Variables for substitution (optional)
        "city", "BANGALORE",
        "year", "2024",
        "sequence", "001"
    ))
    .build();
```

**MDMS Model:**
```java
// MDMS response contains:
mdms.getId()                             // MDMS entry ID
mdms.getUniqueIdentifier()               // Unique identifier
mdms.getSchemaCode()                     // Schema code
mdms.getData()                           // MDMS data object
mdms.getIsActive()                       // Active status
mdms.getAuditDetails()                   // Audit information
```

**Notification Request Models:**
```java
// Email request
SendEmailRequest emailRequest = SendEmailRequest.builder()
    .templateId("EMAIL_TEMPLATE")         // Email template ID (required)
    .version("1.0")                       // Template version
    .emailIds(List.of("email@test.com"))  // Email addresses (required)
    .payload(Map.of("key", "value"))      // Template data
    .enrich(false)                        // Enrichment flag
    .build();

// SMS request
SendSMSRequest smsRequest = SendSMSRequest.builder()
    .templateId("SMS_TEMPLATE")           // SMS template ID (required)
    .version("1.0")                       // Template version
    .mobileNumbers(List.of("9876543210")) // Mobile numbers (required)
    .payload(Map.of("otp", "123456"))     // Template data
    .category(SMSCategory.TRANSACTIONAL)  // SMS category
    .enrich(false)                        // Enrichment flag
    .build();
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
