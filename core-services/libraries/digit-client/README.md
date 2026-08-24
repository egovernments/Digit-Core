# Digit Client Library

A Java Spring client library for Digit platform microservices. Provides strongly-typed clients for all core Digit services with automatic header propagation, optional Redis caching, and Spring Boot 4 auto-configuration.

## Features

- **12 Service Clients**: Workflow, Individual, Filestore, IdGen, MDMS, Notification, Boundary, Billing, Registry, Account, Employee, OTP
- **Package**: `org.digit.*`
- **Spring Boot 4 / Spring Framework 7 / Java 25**
- **Automatic header propagation**: `X-Tenant-ID`, `X-Correlation-ID`, and custom headers forwarded on every outbound call
- **Optional Redis caching**: Registry client caches version/ID lookups to skip redundant searches
- **Auto-configuration**: Drop the JAR on the classpath — clients are wired automatically

---

## Dependency

```xml
<dependency>
    <groupId>org.digit.services</groupId>
    <artifactId>digit-client</artifactId>
    <version>1.1.0-SNAPSHOT</version>
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
digit.services.account.base-url=http://account-service:8094
digit.services.employee.base-url=http://employee-service:8081
digit.services.otp.base-url=http://otp-service:8110

# Timeouts (milliseconds)
digit.services.timeout.connect=5000
digit.services.timeout.read=30000

# Header propagation — headers to forward from incoming to outgoing requests
digit.propagate.headers.allow=authorization,x-correlation-id,x-request-id,x-tenant-id,x-user-id,x-client-id,x-roles
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
- All 12 service client beans
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


Beyond transitions and whole-definition authoring, `WorkflowClient` covers the pieces individually:
`createProcess` / `searchProcesses` / `getProcessByCode` / `updateProcess` / `deleteProcess`; state
CRUD (`createState`, `getState`, `updateState`, `deleteState`); action CRUD (`createAction`,
`listActions`, `getAction`, `updateAction`, `deleteAction`); escalation config CRUD; `escalateNow` and
`searchEscalatable`; `searchTransitions` and `countTransitions`; and `executeSystemTransition`, which
bypasses the role and assignee checks a user transition applies.

Two things to watch: escalation SLAs are in **minutes** while process and state SLAs are in
milliseconds, and `searchTransitions` accepts at most one of `entityId`, `currentState`, `assignee`
and `escalated` — supplying none returns the caller's inbox.

### IndividualClient

Endpoint base: configured via `digit.services.individual.base-url`

**Note**: `tenantId` is resolved from the `X-Tenant-ID` header automatically. The model has no such
field — the service rejects a payload carrying one.

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
    .givenName("Dinesh")
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
| `givenName` | String | Mandatory |
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
| `identifiers` | `List<Identifier>` | PAN, AADHAAR, PASSPORT, … |
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
| `createTemplate(TemplateRequest)` | `POST /notification/v3/template` | Register a template |
| `updateTemplate(TemplateRequest)` | `PUT /notification/v3/template` | Publish a new version |
| `searchTemplates(TemplateSearchCriteria)` / `getTemplate(templateId)` | `GET /notification/v3/template` | Find templates; `ids` travels as one comma-separated param |
| `previewTemplate(TemplatePreviewRequest)` | `POST /notification/v3/template/preview` | Render a template without sending it |
| `deleteTemplate(templateId, version)` | `DELETE /notification/v3/template` | Remove one version |

Templates are versioned: an update publishes a new version rather than editing in place, so messages
already sent keep rendering from the version they used. `SMSCategory` must be one of `OTP`,
`TRANSACTION`, `PROMOTION`, `NOTIFICATION` or `OTHERS`.

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
    .category(SendSMSRequest.SMSCategory.TRANSACTION)
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
| `isValidBoundariesByCodes(List<String> codes, String hierarchyType)` | `GET /boundary/v3/relationship?hierarchyType=...&codes=...` | Validate that all codes exist in the given hierarchy |
| `updateBoundary(String boundaryId, Boundary)` | `PUT /boundary/v3/boundaries/{id}` | Update a boundary |
| `createBoundaryHierarchy(BoundaryHierarchy)` | `POST /boundary/v3/hierarchy` | Create hierarchy definition |
| `searchBoundaryHierarchy(String hierarchyType)` | `GET /boundary/v3/hierarchy?hierarchyType=...` | Find hierarchy by type |
| `createBoundaryRelationship(BoundaryRelationship)` | `POST /boundary/v3/relationship` | Create parent-child relationship |
| `searchBoundaryRelationships(String hierarchyType, String boundaryType, boolean includeChildren)` | `GET /boundary/v3/relationship?...` | Search relationships |
| `updateBoundaryRelationship(String relationshipId, BoundaryRelationship)` | `PUT /boundary/v3/relationship/{id}` | Update a relationship |

#### Usage

The `hierarchyType` parameter in `isValidBoundariesByCodes` must match the hierarchy registered in the boundary service for your tenant (e.g. `ADMIN`). Inject it from config rather than hardcoding:

```java
@Value("${your-service.boundary.hierarchy-type}")
private String boundaryHierarchyType;

// Validate that a boundary code exists in the hierarchy before accepting user input
boolean valid = boundaryClient.isValidBoundariesByCodes(
    List.of("TENANT-BOUNDARIES_BOUNDARY_002"), boundaryHierarchyType);

// Search boundaries by code
BoundarySearchResponse result = boundaryClient.searchBoundariesByCodes(
    List.of("TENANT-BOUNDARIES_BOUNDARY_001", "TENANT-BOUNDARIES_BOUNDARY_002"));
```

Add to your `application.properties`:

```properties
# Must match the hierarchyType registered in the boundary service for your tenant
your-service.boundary.hierarchy-type=ADMIN
```

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


Also available: `getBusinessService` / `updateBusinessService` / `patchBusinessService` /
`deleteBusinessService` and the same four for tax heads, all keyed by **code** rather than id;
`bulkGenerateBills` (answered 202 — the work is queued, not done); `cancelBills` (moves a consumer's
bills to another status); and `validatePayment` (a dry run that persists nothing).

Note the patch shapes are narrower than the updates: a business-service patch cannot change
`partialPaymentAllowed`, and a tax-head patch cannot change `category` or `order` — the service's
patch records simply don't accept those fields.

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
data.setData(DigitJson.mapper().createObjectNode().put("businessName", "Acme"));
RegistryDataResponse created = registryClient.createRegistryData("trade-license", data);

// Search by field
RegistryDataResponse found = registryClient.searchRegistryData(
    "trade-license", "businessName", "Acme");

// Update (uses cache if Redis configured, otherwise searches first)
registryClient.updateRegistryData("trade-license", updatedData, "businessName", "Acme");
```

### AccountClient

Endpoint base: `digit.services.account.base-url`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `searchTenants()` / `searchTenants(name, email, page, size)` / `searchTenants(code, name, email, page, size)` | `GET /accounts/v3/tenants` | List or filter tenants |
| `getTenantByCode(code)` | `GET /accounts/v3/tenants?code=` | Exact-code lookup. `code` identifies a tenant, so prefer this over the name and email variants |
| `getTenantByName(name)` | `GET /accounts/v3/tenants?name=` | Exact-name lookup — the service filter is partial, so this picks the exact match |
| `getTenantByEmail(email)` | `GET /accounts/v3/tenants?email=` | Exact-email lookup |
| `createTenantConfig(request)` | `POST /accounts/v3/config` | Add a config entry for the tenant in context |
| `searchTenantConfigs([configKey, page, size])` | `GET /accounts/v3/config` | List config entries, optionally for one key |
| `getTenantConfig(configKey)` / `getTenantConfigValue(configKey)` | `GET /accounts/v3/config?configKey=` | One entry, or null |
| `updateTenantConfig(id, request)` | `PUT /accounts/v3/config/{id}` | Update an entry |
| `setTenantConfig(key, value, description)` | read, then create or update | Convenience upsert |

The tenant for config calls comes from the request context, not the payload.

> The account service exposes **no delete** for tenant config, and no read-by-id — only create, list
> and update, on both its header-based and canonical routes. `getTenantConfig` is therefore built on
> the list endpoint, and `setTenantConfig` is a read-then-write, so it is not atomic. Tenant create,
> update and delete are intentionally not exposed here.

### EmployeeClient

Endpoint base: `digit.services.employee.base-url`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `createEmployees(list)` / `createEmployee(one)` | `POST /employee/v3/employees` | Create; the endpoint takes an array |
| `onboardEmployee(request)` | `POST /employee/v3/employees/onboard` | Provision login user + individual + employee in one call |
| `searchEmployees(criteria)` | `GET /employee/v3/employees` | Filter on ids, codes, userIds, statuses, types, departments, designations, appointment dates, role, isActive |
| `searchEmployeesByUserIds(userIds)` / `getEmployeeByUserId(userId)` | `GET …?userIds=…` | Find employees by Keycloak user id |
| `searchEmployeesByRole(role, userIds)` | `GET …?role=…&userIds=…` | Role members; with userIds the two **intersect** |
| `getEmployeeById(id)` | `GET /employee/v3/employees/{id}` | |
| `updateEmployee(id, request)` / `patchEmployee(id, request)` | `PUT` / `PATCH …/{id}` | Full or partial update |
| `deleteEmployee(id)` | `DELETE …/{id}` | 204, no body |
| `deactivateEmployee(id)` / `reactivateEmployee(id)` | `POST …/{id}/deactivate` / `/reactivate` | |
| `createJurisdiction` / `searchJurisdictions` / `getJurisdiction` / `updateJurisdiction` | `…/{employeeId}/jurisdictions[/{id}]` | Jurisdiction CRUD |

`role` is resolved through Keycloak, so those searches need a bearer token.

> The `userIds` filter reached the employee service more recently than the rest. Against a deployment
> that predates it the parameter is **ignored rather than rejected**, which means an unfiltered result
> instead of an error. Check the service version if a search looks too broad.

### OtpClient

Endpoint base: `digit.services.otp.base-url`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `generateOtp(identifier, purpose)` | `POST /otp/v3/generate` | Issue a code; returns a reference, never the code |
| `verifyOtp(request)` / `isOtpValid(referenceId, otp)` | `POST /otp/v3/verify` | Check a code |
| `resendOtp(referenceId)` | `POST /otp/v3/resend` | Re-send, subject to cooldown and hourly cap |
| `invalidateOtp(referenceId)` | `POST /otp/v3/invalidate` | Retire a code early |
| `createOtpConfig(config)` | `POST /otp/v3/config` | Define policy for a purpose |
| `listOtpConfigs()` | `GET /otp/v3/config` | All purposes |
| `getOtpConfig(purpose)` | `GET /otp/v3/config?purpose=` | One purpose, or null |
| `updateOtpConfig(id, config)` | `PUT /otp/v3/config?id=` | Note: id is a **query** parameter |
| `deleteOtpConfig(purpose)` | `DELETE /otp/v3/config?purpose=` | Identified by purpose, not id |

A wrong code is a normal answer (`verified: false`), not an exception. Lockout after too many
attempts, or an expired reference, is an error from the service.

> `GET /otp/v3/config` is polymorphic — an array without `purpose`, a single object with it — which is
> why `listOtpConfigs()` and `getOtpConfig(purpose)` are separate methods.

---

## Bootstrap and admin writes

The operations you call once to set a tenant or service up, as opposed to the runtime calls that then
depend on them.

| Client | Method | Endpoint |
|---|---|---|
| `IdGenClient` | `createTemplate` / `updateTemplate` | `POST` / `PUT /idgen/v3/template` |
| | `searchTemplates` / `getTemplate` / `templateExists` | `GET /idgen/v3/template` |
| | `deleteTemplate(code, version)` | `DELETE /idgen/v3/template` |
| | `generateIds(code, count[, variables])` | `POST /idgen/v3/generate/bulk` |
| `WorkflowClient` | `createProcessDefinition` / `updateProcessDefinition` | `POST` / `PUT /workflow/v3/process/definition` |
| | `listProcessDefinitions` / `getProcessDefinition(code, version)` | `GET /workflow/v3/process/definition` |
| | `deleteProcessDefinition` | `DELETE /workflow/v3/process/definition/{code}` |
| `BillingClient` | `createBusinessServices` / `createBusinessService` | `POST /billing/v3/business-services` |
| | `createTaxHeads` / `createTaxHead` | `POST /billing/v3/tax-heads` |
| `RegistryClient` | `createSchema` / `updateSchema` / `deleteSchema` | `POST` / `PUT` / `DELETE /registry/v3/schema` |
| `FilestoreClient` | `createDocumentCategory` / `updateDocumentCategory` / `deleteDocumentCategory` | `/filestore/v3/document-categories` |
| `MdmsClient` | `createSchema` / `createMasterData` | `POST /mdms-v2/v1/schema`, `POST /mdms-v2/v2` |

Things worth knowing before you call these:

- **idgen templates are versioned and immutable.** A create makes `v1`; an update publishes `v(n+1)`
  rather than editing what exists. The sequence counter belongs to the template *code*, so updating a
  template does not restart numbering. `deleteTemplate` removes one version and therefore requires
  both arguments.
- **Prefer `generateIds` to a loop.** It reserves the whole block against the sequence in one call.
- **Workflow definitions are validated locally first**, against the same rules the service applies:
  code and name required, at least one state, exactly one `INITIAL`, unique state codes, unique action
  codes within a state, and every `nextState` resolving to a declared state code. On the way *in*
  `nextState` is a state code; the definition you read back has ids there instead. There is no
  canonical route for this operation — header-based only.
- **Billing catalogue creates take arrays** and return a `BulkResult`, so a batch can partly succeed.
  `billExpiryDays`, `currency`, `effectiveFrom` and `isActive` are required on a business service;
  `order` (≥1) and `businessServiceCode` on a tax head. Tax-head `order` serializes as `order`.
- **Registry schema writes send hyphenated keys** — `x-unique`, `x-ref-schema`, `x-indexes` — while
  reads return them camel-cased, which is why `RegistrySchemaRequest` and `RegistrySchema` are
  separate types. An update publishes a new version; records already stored keep validating against
  the version they were written under.
- **MDMS writes are wrapped**: `{"SchemaDefinition": {...}}` and `{"Mdms": {...}}`, with the schema
  body under `definition`. That follows the client with evidence of working against a live server; the
  published spec contradicts itself on this field. Both need `X-Client-ID`.

Bootstrap flows usually run outside a servlet request, so supply a `DigitRequestContext` — see
[Calls made outside a request](#calls-made-outside-a-request).

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
digit.propagate.headers.allow=authorization,x-correlation-id,x-request-id,x-tenant-id,x-user-id,x-client-id,x-roles
digit.propagate.headers.prefixes=x-ctx-,x-trace-
```

This means **you never need to pass `tenantId` as a method parameter** — it flows automatically via `X-Tenant-ID`.

`x-user-id` is not optional: billing and workflow reject every write without it, and registry rejects
every call including reads. If you override `digit.propagate.headers.allow`, keep it in the list.

### Calls made outside a request

Header propagation only works while there is an inbound servlet request to copy from. In a Kafka
consumer, an `@Async` method, a scheduled job, or bootstrap code acting as a newly created tenant's
own user, there is nothing to copy — so supply the context explicitly:

```java
DigitContextHolder.run(
        DigitRequestContext.builder()
                .tenantId("PB")
                .userId("2c9a...")          // required by billing, workflow and registry
                .clientId("svc-license")    // required by MDMS only
                .authToken(token)
                .build(),
        () -> registryClient.createRegistryData("Trade.License", data));
```

Precedence is: headers you set on the call yourself, then `DigitContextHolder`, then the inbound
request. Setting nothing keeps the previous behaviour exactly.

> **Keycloak users:** with no `X-Tenant-ID` header the library falls back to a `realm` claim in the
> JWT, which Keycloak does not issue — the realm is in the `iss` URL, not a claim. Either send
> `X-Tenant-ID` on the inbound request or supply a `DigitRequestContext`.

> **MDMS:** it is the only service requiring `X-Client-ID`, and no gateway synthesizes it, so the
> caller must supply it — via the context above or an inbound `X-Client-Id` header. `MdmsClient`
> fails fast with a clear message rather than letting the service reject the call.

### Auto-registration on other RestTemplates

The library's own clients use its `digitRestTemplate`. Earlier versions also attached the interceptor
to *every* `RestTemplate` bean in the context, which sent your tenant, user and `Authorization`
headers to third-party hosts too. That is now off unless you ask for it:

```properties
digit.propagate.auto-register-all-rest-templates=true
```

---

## Redis Caching (Registry)

When `spring.cache.type=redis` is configured, `RegistryClient` caches `{registryId, version}` entries:

- **On create**: cache is populated with the server response
- **On update**: cache hit skips the pre-update search; version is taken from the update response
- **Cache key format**: `registry:{schemaCode}:{tenantId}:{key}:{value}`

If Redis is not configured the client falls back to the original search-before-update behavior.

> **Spring Boot 4 — conditional `CacheManager` beans:** Spring Boot 4 fails fast if a `RedisCacheManager` bean is present but no Redis connection is available, even when `spring.cache.type=simple`. If your service defines its own `@Configuration` class that creates a `CacheManager` bean, make the Redis variant conditional:
>
> ```java
> @Bean
> @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
> public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) { ... }
>
> @Bean
> @ConditionalOnMissingBean(CacheManager.class)
> public CacheManager simpleCacheManager() {
>     return new ConcurrentMapCacheManager(...);
> }
> ```
>
> Without this, setting `spring.cache.type=simple` in `application.properties` has no effect and the app fails to start if Redis is not running.

---

## Building

```bash
# Requires Java 25
mvn clean test
```
