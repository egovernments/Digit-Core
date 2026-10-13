# Digit Client Library

A Java Spring client library for Digit platform microservices. Provides strongly-typed clients for all core Digit services with automatic header propagation, optional Redis caching, and Spring Boot 4 auto-configuration.

## Features

- **9 Service Clients**: Workflow, Individual, Filestore, IdGen, MDMS, Notification, Boundary, Billing, Registry
- **Package**: `org.digit.*`
- **Spring Boot 4 / Spring Framework 7 / Java 21**
- **Automatic header propagation**: `X-Tenant-ID`, `X-Correlation-ID`, and custom headers forwarded on every outbound call
- **Optional Redis caching**: Registry client caches version/ID lookups to skip redundant searches
- **Auto-configuration**: Drop the JAR on the classpath — clients are wired automatically

---

## Dependency

```xml
<dependency>
    <groupId>org.digit.services</groupId>
    <artifactId>digit-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## Configuration

Add to your `application.properties`:

```properties
# Service base URLs
digit.services.workflow.base-url=http://workflow-service:8085
digit.services.individual.base-url=http://individual-service:8999
digit.services.filestore.base-url=http://filestore-service:8080
digit.services.idgen.base-url=http://idgen-service:8100
digit.services.mdms.base-url=http://mdms-service:8080
digit.services.notification.base-url=http://notification-service:8091
digit.services.boundary.base-url=http://boundary-service:8080
digit.services.billing.base-url=http://billing-service:8080
digit.services.registry.base-url=http://registry-service:8085

# Timeouts (milliseconds)
digit.services.timeout.connect=5000
digit.services.timeout.read=30000

# Header propagation — headers to forward from incoming to outgoing requests
digit.propagate.headers.allow=authorization,x-correlation-id,x-request-id,x-tenant-id,x-user-id
digit.propagate.headers.prefixes=x-ctx-,x-trace-

# Redis caching for RegistryClient (optional — omit to disable)
spring.data.redis.host=localhost
spring.data.redis.port=6380
spring.cache.type=redis
```

All service URL properties default to `http://localhost:808x` if not set.

---

## Auto-configuration

Spring Boot 4 picks up the library via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. No `@Import` or manual bean wiring needed. The following are auto-configured:

- `RestTemplate` (with header propagation interceptor and error handler)
- All 9 service client beans
- `RegistryCacheAutoConfiguration` (only when `spring.cache.type=redis` is set)

---

## Service Clients

### WorkflowClient

Endpoint base: configured via `digit.services.workflow.base-url`

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `executeTransition(WorkflowTransitionRequest)` | `POST /workflow/v3/transition` | Execute a workflow transition with a full request object |
| `executeTransition(String processCode, String entityId, String action, String comment)` | `POST /workflow/v3/transition` | Shorthand transition — builds request internally |
| `executeTransition(String processCode, String entityId, String action, String comment, Map<String, List<String>> attributes)` | `POST /workflow/v3/transition` | Transition with extra attributes |
| `listStates(String processCode)` | `GET /workflow/v3/process/{processCode}/state` | List all states for a process |
| `getProcessDefinition(String processCode)` | `GET /workflow/v3/process/definition/{processCode}` | Get process definition by code |

#### Usage

```java
// Simple transition
WorkflowTransitionResponse resp = workflowClient.executeTransition(
    "CITIZEN_REGISTRATION", "entity-456", "APPROVE", "Looks good");

// Full request
WorkflowTransitionRequest req = WorkflowTransitionRequest.builder()
    .processCode("CITIZEN_REGISTRATION")
    .entityId("entity-456")
    .action("APPROVE")
    .comment("Approved")
    .build();
workflowClient.executeTransition(req);

// List states
List<WorkflowState> states = workflowClient.listStates("CITIZEN_REGISTRATION");

// Get process definition
WorkflowProcessResponse process = workflowClient.getProcessDefinition("CITIZEN_REGISTRATION");
```

Required fields for transition: `processCode`, `entityId`, `action`.

---

### IndividualClient

Endpoint base: configured via `digit.services.individual.base-url`

**Note**: `tenantId` is resolved from the `X-Tenant-ID` header automatically — never pass it as a parameter.

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `createIndividual(Individual)` | `POST /individuals/v3/individuals` | Create a new individual record |
| `getIndividualById(String id)` | `GET /individuals/v3/individuals/{id}` | Fetch individual by system UUID |
| `searchIndividualsByName(String givenName)` | `GET /individuals/v3/individuals?givenName=...` | Search by name, default page=1 size=20 |
| `searchIndividualsByName(String givenName, Integer page, Integer size)` | `GET /individuals/v3/individuals?givenName=...` | Search by name with pagination |
| `searchAllIndividuals()` | `GET /individuals/v3/individuals` | All individuals, default page=1 size=20 |
| `searchAllIndividuals(Integer page, Integer size)` | `GET /individuals/v3/individuals` | All individuals with custom pagination |
| `isIndividualExist(String id)` | `GET /individuals/v3/individuals/exists?id=...` | Check existence, returns boolean |
| `isIndividualExistsById(String id, Integer page, Integer size)` | `GET /individuals/v3/individuals/exists?id=...` | Check existence (page/size ignored by server) |

Pagination is 1-indexed. Default: page=1, size=20, max size=100.

#### Usage

```java
// Create
Individual created = individualClient.createIndividual(Individual.builder()
    .name("Dinesh")
    .gender("MALE")
    .mobileNumber("9800000010")
    .build());

// Get by ID
Individual ind = individualClient.getIndividualById("3fa85f64-5717-4562-b3fc-2c963f66afa6");

// Search
IndividualSearchResponse result = individualClient.searchIndividualsByName("Dinesh", 1, 20);
List<Individual> individuals = result.getIndividuals();
Long total = result.getTotalCount();
Boolean hasMore = result.getHasMore();

// Existence check
boolean exists = individualClient.isIndividualExist("3fa85f64-5717-4562-b3fc-2c963f66afa6");
```

#### Individual model fields

| Field | Type | Notes |
|-------|------|-------|
| `id` | String | System UUID — server-generated, read-only |
| `individualId` | String | IDGen stable external ID — read-only |
| `tenantId` | String | Read-only — resolved from Bearer token |
| `name` | String | `givenName` in JSON — mandatory |
| `familyName` | String | Optional |
| `otherNames` | String | Optional |
| `gender` | String | `MALE` / `FEMALE` / `OTHER` — mandatory |
| `dateOfBirth` | String | `YYYY-MM-DD` |
| `age` | Integer | Optional |
| `mobileNumber` | String | Mandatory if email not provided |
| `mobileNumberVerified` | Boolean | |
| `altContactNumber` | String | |
| `email` | String | Mandatory if mobileNumber not provided |
| `emailVerified` | Boolean | |
| `locale` | String | BCP 47 (e.g. `en-IN`) |
| `fatherName` | String | |
| `husbandName` | String | |
| `photo` | String | File-store reference ID |
| `userId` | String | Optional User Service link |
| `isActive` | Boolean | `false` after soft-delete — read-only |
| `version` | Integer | Optimistic lock version — read-only |
| `address` | `List<Address>` | |
| `documents` | `List<Document>` | |
| `additionalAttributes` | `Map<String, String>` | Max 50 entries |
| `auditDetail` | AuditDetails | |

---

### FilestoreClient

Endpoint base: configured via `digit.services.filestore.base-url`

**Note**: `tenantId` is propagated automatically via `X-Tenant-ID` header — not a parameter.

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `isFileAvailable(String fileId)` | `GET /filestore/v3/files/{fileId}` | Returns `false` for any error (safe) |
| `validateFileAvailability(String fileId)` | `GET /filestore/v3/files/{fileId}` | Throws `DigitClientException` on 4xx errors |

#### Usage

```java
// Safe check — never throws, returns false on any error
boolean available = filestoreClient.isFileAvailable("fs-abc-123");

// Strict check — throws DigitClientException on 403/404/400
boolean valid = filestoreClient.validateFileAvailability("fs-abc-123");
```

---

### IdGenClient

Endpoint base: configured via `digit.services.idgen.base-url`

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `generateId(IdGenGenerateRequest)` | `POST /idgen/v3/generate` | Generate ID with full request |
| `generateId(String templateCode, Map<String, String> variables)` | `POST /idgen/v3/generate` | Generate ID with template + variables |
| `generateId(String templateCode)` | `POST /idgen/v3/generate` | Generate ID with template code only |

#### Usage

```java
String id = idGenClient.generateId("RECEIPT_ID");

String id = idGenClient.generateId("RECEIPT_ID",
    Map.of("city", "BANGALORE", "year", "2025"));
```

---

### MdmsClient

Endpoint base: configured via `digit.services.mdms.base-url`

**Note**: Tenant is propagated via `X-Tenant-ID` header automatically.

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `isMdmsDataValid(String schemaCode, Set<String> uniqueIdentifiers)` | `GET /mdms-v2/v2?schemaCode=...&uniqueIdentifiers=...` | Returns `true` if all identifiers exist |
| `searchMdmsData(String schemaCode, Set<String> uniqueIdentifiers)` | `GET /mdms-v2/v2?schemaCode=...&uniqueIdentifiers=...` | Returns list of matching MDMS entries |

#### Usage

```java
boolean valid = mdmsClient.isMdmsDataValid(
    "common-masters.PropertyType", Set.of("RESIDENTIAL", "COMMERCIAL"));

List<Mdms> data = mdmsClient.searchMdmsData(
    "common-masters.PropertyType", Set.of("RESIDENTIAL"));
```

---

### NotificationClient

Endpoint base: configured via `digit.services.notification.base-url`

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `sendEmail(SendEmailRequest)` | `POST /notification/v3/email/send` | Send email with full request |
| `sendEmail(String templateId, String version, List<String> emailIds, Map<String, Object> payload)` | `POST /notification/v3/email/send` | Shorthand email send |
| `sendSMS(SendSMSRequest)` | `POST /notification/v3/sms/send` | Send SMS with full request |
| `sendSMS(String templateId, String version, List<String> mobileNumbers, Map<String, Object> payload, SMSCategory category)` | `POST /notification/v3/sms/send` | Shorthand SMS send |

#### Usage

```java
// Email
notificationClient.sendEmail(SendEmailRequest.builder()
    .templateId("WELCOME_EMAIL")
    .emailIds(List.of("user@example.com"))
    .payload(Map.of("name", "Dinesh"))
    .build());

// SMS
notificationClient.sendSMS(SendSMSRequest.builder()
    .templateId("OTP_SMS")
    .mobileNumbers(List.of("9800000010"))
    .payload(Map.of("otp", "123456"))
    .category(SendSMSRequest.SMSCategory.TRANSACTIONAL)
    .build());
```

---

### BoundaryClient

Endpoint base: configured via `digit.services.boundary.base-url`

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `createBoundaries(List<Boundary>)` | `POST /boundary/v3/boundaries` | Create boundary records |
| `searchBoundariesByCodes(List<String> codes)` | `GET /boundary/v3/boundaries?codes=...` | Search boundaries by codes |
| `isValidBoundariesByCodes(List<String> codes)` | `GET /boundary/v3/boundaries?codes=...` | Validate that all codes exist |
| `updateBoundary(String boundaryId, Boundary)` | `PUT /boundary/v3/boundaries/{id}` | Update a boundary |
| `createBoundaryHierarchy(BoundaryHierarchy)` | `POST /boundary/v3/hierarchy` | Create hierarchy definition |
| `searchBoundaryHierarchy(String hierarchyType)` | `GET /boundary/v3/hierarchy?hierarchyType=...` | Find hierarchy by type |
| `createBoundaryRelationship(BoundaryRelationship)` | `POST /boundary/v3/relationship` | Create parent-child relationship |
| `searchBoundaryRelationships(String hierarchyType, String boundaryType, boolean includeChildren)` | `GET /boundary/v3/relationship?...` | Search relationships |
| `updateBoundaryRelationship(String relationshipId, BoundaryRelationship)` | `PUT /boundary/v3/relationship/{id}` | Update a relationship |

---

### BillingClient

Endpoint base: configured via `digit.services.billing.base-url`

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `createDemand(DemandCreate)` | `POST /billing/v3/demands` | Create a demand |
| `searchDemands(String businessService, String consumerCode)` | `GET /billing/v3/demands?...` | Search demands |
| `generateBill(GenerateBillCriteria)` | `POST /billing/v3/bills/generate` | Generate bill from demand |
| `createPayment(PaymentCreate)` | `POST /billing/v3/payments` | Record a payment |

---

### RegistryClient

Endpoint base: configured via `digit.services.registry.base-url`

Supports optional Redis caching to avoid redundant search-before-update calls. Cache is keyed by `registry:{schemaCode}:{tenantId}:{key}:{value}` and populated on create/update.

#### Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `createRegistryData(String schemaCode, RegistryData)` | `POST /registry/v3/{schemaCode}/data` | Create a registry entry; populates cache |
| `searchRegistryData(String schemaCode, String registryId, boolean history)` | `GET /registry/v3/{schemaCode}/data/_registry?registryId=...` | Fetch by registry ID |
| `searchRegistryData(String schemaCode, String registryId)` | same, history=false | Shorthand without history |
| `searchRegistryData(String schemaCode, String key, String value)` | `POST /registry/v3/{schemaCode}/data/_search` | Search by field value |
| `searchRegistryData(String schemaCode, String key, String value, Integer limit, Integer offset)` | same with pagination | Search with limit/offset |
| `updateRegistryData(String schemaCode, RegistryData, String key, String value)` | `PUT /registry/v3/{schemaCode}/data?id=...` | Update; uses cache to skip search, updates cache after |

#### Constructors

```java
// Without Redis caching
RegistryClient client = new RegistryClient(restTemplate, apiProperties);

// With Redis caching (auto-wired when spring.cache.type=redis)
RegistryClient client = new RegistryClient(restTemplate, apiProperties, redisTemplate);
```

#### Usage

```java
// Create
RegistryData data = new RegistryData();
data.setData(objectMapper.createObjectNode().put("businessName", "Acme"));
RegistryDataResponse created = registryClient.createRegistryData("trade-license", data);

// Search by field
RegistryDataResponse found = registryClient.searchRegistryData(
    "trade-license", "businessName", "Acme");

// Update (uses cache if Redis configured, otherwise searches first)
registryClient.updateRegistryData("trade-license", updatedData, "businessName", "Acme");
```

---

## Error Handling

All client methods throw `DigitClientException` on:
- Null/empty required parameters (thrown before the HTTP call)
- Non-2xx HTTP responses
- Network or serialization failures

```java
try {
    Individual ind = individualClient.getIndividualById("abc-123");
} catch (DigitClientException e) {
    log.error("Failed: {}", e.getMessage(), e);
}
```

`FilestoreClient.isFileAvailable()` is the one exception — it catches all errors and returns `false` instead of throwing.

---

## Header Propagation

The `HeaderPropagationInterceptor` automatically copies headers from the incoming servlet request to every outbound `RestTemplate` call. Configure which headers to propagate:

```properties
digit.propagate.headers.allow=authorization,x-correlation-id,x-request-id,x-tenant-id,x-user-id
digit.propagate.headers.prefixes=x-ctx-,x-trace-
```

This means **you never need to pass `tenantId` as a method parameter** — it flows automatically via `X-Tenant-ID`.

---

## Redis Caching (Registry)

When `spring.cache.type=redis` is configured, `RegistryClient` caches `{registryId, version}` entries:

- **On create**: cache is populated with the server response
- **On update**: cache hit skips the pre-update search; version is taken from the update response
- **Cache key format**: `registry:{schemaCode}:{tenantId}:{key}:{value}`

If Redis is not configured the client falls back to the original search-before-update behavior.

---

## Building

```bash
# Requires Java 21
mvn clean test
```
