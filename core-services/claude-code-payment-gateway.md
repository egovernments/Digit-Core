# Claude Code Prompt — Create `payment-gateway` service with Java SPI

## Your first instruction

The user will provide the path to the existing `pg-service` source code when invoking
this prompt. Refer to it throughout as `$PG_SERVICE_PATH`. Before writing a single
file, read the following from `$PG_SERVICE_PATH`:

1. `CLAUDE.md` — architecture overview, transaction lifecycle, gateway plugin pattern
2. `pom.xml` — dependencies, Spring Boot version, Java version, DIGIT library versions
3. `src/main/java/org/egov/pg/service/Gateway.java` — existing gateway interface
4. `src/main/java/org/egov/pg/service/GatewayService.java` — how gateways are discovered today
5. `src/main/java/org/egov/pg/models/Transaction.java` — core domain model
6. `src/main/java/org/egov/pg/models/TransactionStatus.java` — status model
7. All four gateway implementations: `AxisGateway`, `PaytmGateway`, `PhonePeGateway`, `PayuGateway`
8. `src/main/java/org/egov/pg/clients/` — all DIGIT service clients
9. `src/main/java/org/egov/pg/service/TransactionService.java` — full transaction lifecycle
10. `src/main/java/org/egov/pg/service/EnrichmentService.java`
11. `src/main/java/org/egov/pg/service/validator/TransactionValidator.java`
12. `src/main/resources/application.properties` — all config keys and their structure
13. `src/main/resources/db/migration/main/` — all Flyway migration files
14. `src/main/java/org/egov/pg/web/controllers/` — REST controllers and request/response models

Record your findings in `READING-NOTES.md` at the new service root before
proceeding. This file is for your own reference — document every domain model,
config key, client URL, and gateway-specific detail you will need to carry over.

---

## Overview

Create a brand new Spring Boot service called `payment-gateway` at the path where
this prompt is being run. It is a **clean reimplementation** of `pg-service` —
not a copy. Carry over domain logic, models, DIGIT integrations, and gateway
behaviour, but rebuild the architecture with a formal Java SPI layer from day one.

```
payment-gateway/
├── pom.xml
├── CLAUDE.md
├── READING-NOTES.md             ← your notes from reading pg-service
├── DECISIONS.md                 ← your architectural decisions (see Step 9)
└── src/
    ├── main/
    │   ├── java/org/egov/
    │   │   ├── gateway/          ← SPI contract (zero Spring dependency)
    │   │   │   ├── spi/
    │   │   │   │   ├── GatewayProviderFactory.java
    │   │   │   │   ├── GatewayProvider.java
    │   │   │   │   ├── GatewayException.java
    │   │   │   │   └── model/
    │   │   │   │       └── GatewayProviderConfig.java
    │   │   └── payment/          ← application code (Spring Boot)
    │   │       ├── PaymentGatewayApplication.java
    │   │       ├── config/
    │   │       ├── models/
    │   │       ├── repository/
    │   │       ├── service/
    │   │       │   ├── TransactionService.java
    │   │       │   ├── EnrichmentService.java
    │   │       │   ├── PaymentsService.java
    │   │       │   ├── registry/
    │   │       │   │   └── GatewayProviderRegistry.java
    │   │       │   ├── gateways/
    │   │       │   │   ├── axis/
    │   │       │   │   ├── paytm/
    │   │       │   │   ├── phonepe/
    │   │       │   │   └── payu/
    │   │       │   └── jobs/
    │   │       ├── validator/
    │   │       ├── clients/
    │   │       └── web/
    │   └── resources/
    │       ├── application.properties
    │       ├── db/migration/main/
    │       └── META-INF/services/
    │           └── org.egov.gateway.spi.GatewayProviderFactory
    └── test/
```

---

## Step 1: Create `pom.xml`

Base it on `$PG_SERVICE_PATH/pom.xml`. Use the **same versions** for:
- Spring Boot
- Java (17)
- All `org.egov` / DIGIT library dependencies (tracer, mdms-client, etc.)
- Flyway, PostgreSQL driver, Quartz, OpenTelemetry JDBC wrapper

**Remove**: any dependency that was in pg-service but is not needed in a clean build.
**Add nothing new** — only what is actually used in this implementation.

The SPI contract package (`org.egov.gateway.spi`) must have **zero Spring
dependency**. Enforce this with a Maven `<dependency>` scope comment at minimum.
If you use multi-module Maven, split it into `payment-gateway-api` (SPI only) and
`payment-gateway` (application). If single-module, document in `DECISIONS.md` why.

artifactId: `payment-gateway`
groupId: `org.egov`
base package: `org.egov`
server port: `8080`
context path: `/payment-gateway`

---

## Step 2: SPI contract — `org.egov.gateway.spi`

These four types are the heart of the system. They must compile with zero Spring
on the classpath. No `@Component`, no `@Value`, no `@Autowired` anywhere in this
package.

### `GatewayProviderFactory.java`

```java
/**
 * SPI entry point for a payment gateway provider.
 *
 * One instance per JVM, created once at startup by GatewayProviderRegistry
 * via ServiceLoader. Declares the provider's identity, required config, and
 * creates GatewayProvider instances.
 *
 * Registration: include a file at
 *   META-INF/services/org.egov.gateway.spi.GatewayProviderFactory
 * containing the fully-qualified class name of your implementation.
 */
public interface GatewayProviderFactory {

    /** Unique lowercase hyphen-separated id. e.g. "axis", "paytm", "phonepe", "payu" */
    String getGatewayId();

    /** Human-readable name for logs and admin tooling. */
    String getDisplayName();

    /** Semver string. e.g. "1.0.0" */
    String getVersion();

    /**
     * Declares every config property this provider reads. The registry uses
     * this list to validate application.properties at startup — fail fast if
     * any required key is absent.
     */
    List<GatewayProviderConfig> getConfigProperties();

    /**
     * Called once after instantiation, before any create() calls.
     * @param config  resolved and validated config map for this provider
     */
    default void init(Map<String, String> config) {}

    /**
     * Creates a GatewayProvider for one transaction's lifecycle.
     * @param config  resolved config map (only keys declared in getConfigProperties())
     */
    GatewayProvider create(Map<String, String> config);

    /** Called on application shutdown. Release shared resources here. */
    default void close() {}
}
```

### `GatewayProvider.java`

```java
/**
 * Handles one payment gateway's protocol for a single transaction lifecycle.
 *
 * Created per-transaction by its GatewayProviderFactory. Must be stateless
 * with respect to transaction data — all transaction state lives in the
 * Transaction object passed to each method.
 *
 * No Spring annotations. No framework dependency of any kind.
 */
public interface GatewayProvider {

    String getGatewayId();

    /**
     * Builds the redirect URI the user is sent to for payment.
     * Must handle amount conversion from rupees (Transaction.txnAmount)
     * to the gateway's required unit (e.g. paise = rupees × 100).
     * Must compute any required checksum or hash.
     *
     * @param transaction  the initiated transaction (never null)
     * @return fully-formed redirect URI (never null or empty)
     * @throws GatewayException on any failure
     */
    URI generateRedirectURI(Transaction transaction);

    /**
     * Fetches live transaction status from the gateway API.
     * Must convert gateway-specific response fields to TransactionStatus.
     *
     * @param transaction        the transaction to check
     * @param responseParameters raw parameters from gateway callback
     * @return current TransactionStatus
     * @throws GatewayException on any failure
     */
    TransactionStatus fetchStatus(Transaction transaction,
                                  Map<String, String> responseParameters);

    /**
     * The key in the gateway callback response that holds the gateway's
     * own transaction reference. Used for reconciliation.
     */
    String transactionIdKeyInResponse();
}
```

### `GatewayProviderConfig.java`

```java
/**
 * Declares one configuration property required by a GatewayProviderFactory.
 * Used by GatewayProviderRegistry for startup validation.
 */
public final class GatewayProviderConfig {

    public enum Type { STRING, SECRET, BOOLEAN, INTEGER }

    private final String key;          // e.g. "axis.merchant.id"
    private final String description;  // shown in startup validation errors
    private final Type type;
    private final boolean required;
    private final String defaultValue; // null means no default

    public GatewayProviderConfig(String key, String description,
                                  Type type, boolean required,
                                  String defaultValue) { ... }

    // getters only — immutable
}
```

### `GatewayException.java`

```java
/**
 * Thrown by GatewayProvider methods to signal a gateway-level error.
 * The message must be safe to log — must not contain secrets or PII.
 */
public class GatewayException extends RuntimeException {

    public enum ErrorType {
        CONFIGURATION_ERROR,   // bad/missing config — fail at startup
        CONNECTIVITY_ERROR,    // network/timeout — retry may help
        AUTHENTICATION_ERROR,  // bad credentials — do not retry
        INVALID_RESPONSE,      // gateway returned unexpected data
        TRANSACTION_NOT_FOUND  // gateway has no record of this txnId
    }

    private final ErrorType errorType;
    private final String gatewayId;

    // constructor, getters
}
```

---

## Step 3: Domain models — `org.egov.payment.models`

Carry over from `$PG_SERVICE_PATH/src/main/java/org/egov/pg/models/` but
**clean as you go**:

- `Transaction` — keep all fields. Remove any Spring/Jackson annotations only if
  they are truly unused. Preserve JSONB-mapped `additionalDetails` field.
- `TransactionStatus` — keep exactly as-is; it is used in `GatewayProvider.fetchStatus`.
- `TransactionRequest` / `TransactionResponse` — carry over for REST layer.
- `TaxAndPayment`, `Bill`, `User` — carry over only what `Transaction` or the
  DIGIT clients actually reference.

Do not invent new models. Do not rename fields — DB column names depend on them.

---

## Step 4: Database — same schema, clean migrations

Copy Flyway migrations from `$PG_SERVICE_PATH/src/main/resources/db/migration/main/`
verbatim. Do not alter table names or column names.

Tables:
- `eg_pg_transactions` — transaction state
- `eg_pg_transactions_dump` — raw gateway audit log

In `application.properties`, configure **two datasource URLs** exactly as
pg-service does:
- `spring.datasource.url` — uses OpenTelemetry JDBC wrapper:
  `jdbc:otel:postgresql://${db.host}:${db.port}/${db.name}`
- `spring.flyway.url` — plain PostgreSQL (Flyway runs outside OTel):
  `jdbc:postgresql://${db.host}:${db.port}/${db.name}`

Reproduce the `TransactionRepository` using plain `JdbcTemplate` with inline SQL.
Carry over `TransactionQueryBuilder` and `TransactionRowMapper` — these are
pure JDBC, no ORM.

---

## Step 5: `GatewayProviderRegistry` — the SPI core

Package: `org.egov.payment.service.registry`
Annotation: `@Component`

This is the most important class in the service. Get it exactly right.

```java
@Component
public class GatewayProviderRegistry {

    private final Environment env;
    private Map<String, GatewayProviderFactory> registry; // immutable after init

    @PostConstruct
    public void init() {
        // 1. Discover all factories via ServiceLoader
        Map<String, GatewayProviderFactory> discovered = new LinkedHashMap<>();
        ServiceLoader.load(GatewayProviderFactory.class).forEach(factory -> {
            if (discovered.containsKey(factory.getGatewayId())) {
                throw new IllegalStateException(
                    "Duplicate gateway provider registered for id: "
                    + factory.getGatewayId()
                    + " — conflicting factories: "
                    + discovered.get(factory.getGatewayId()).getClass().getName()
                    + " vs " + factory.getClass().getName());
            }
            discovered.put(factory.getGatewayId(), factory);
        });

        // 2. Validate: every active gateway must have a registered factory
        List<String> activeIds = resolveActiveGatewayIds();
        List<String> missing = activeIds.stream()
            .filter(id -> !discovered.containsKey(id))
            .collect(toList());
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                "Active gateways have no registered provider factory: " + missing
                + ". Available factories: " + discovered.keySet()
                + ". Check META-INF/services/"
                + "org.egov.gateway.spi.GatewayProviderFactory");
        }

        // 3. Validate: all required config present for active gateways
        Map<String, List<String>> missingConfig = new LinkedHashMap<>();
        for (String id : activeIds) {
            GatewayProviderFactory factory = discovered.get(id);
            List<String> missingKeys = factory.getConfigProperties().stream()
                .filter(GatewayProviderConfig::isRequired)
                .map(GatewayProviderConfig::getKey)
                .filter(key -> !env.containsProperty(key))
                .collect(toList());
            if (!missingKeys.isEmpty()) missingConfig.put(id, missingKeys);
        }
        if (!missingConfig.isEmpty()) {
            throw new IllegalStateException(
                "Missing required config properties for active gateways: "
                + missingConfig);
        }

        // 4. Init active factories only
        for (String id : activeIds) {
            GatewayProviderFactory factory = discovered.get(id);
            factory.init(resolveConfig(factory));
        }

        this.registry = Collections.unmodifiableMap(discovered);

        // 5. Startup summary log
        log.info("GatewayProviderRegistry initialised. "
            + "Registered: {} | Active: {} | Inactive: {}",
            discovered.keySet(), activeIds,
            discovered.keySet().stream()
                .filter(id -> !activeIds.contains(id)).collect(toList()));
    }

    @PreDestroy
    public void shutdown() {
        registry.values().forEach(factory -> {
            try { factory.close(); }
            catch (Exception e) {
                log.warn("Error closing factory {}: {}", factory.getGatewayId(), e.getMessage());
            }
        });
    }

    public GatewayProviderFactory getFactory(String gatewayId) {
        GatewayProviderFactory f = registry.get(gatewayId);
        if (f == null) throw new GatewayException(
            GatewayException.ErrorType.CONFIGURATION_ERROR, gatewayId,
            "No factory registered for gateway: " + gatewayId
            + ". Available: " + registry.keySet());
        return f;
    }

    public boolean isActive(String gatewayId) {
        return resolveActiveGatewayIds().contains(gatewayId);
    }

    public Set<String> getActiveGatewayIds() {
        return Set.copyOf(resolveActiveGatewayIds());
    }

    private List<String> resolveActiveGatewayIds() {
        // Read {id}.active=true from application.properties for every
        // discovered factory id. Return only those set to true.
    }

    private Map<String, String> resolveConfig(GatewayProviderFactory factory) {
        // Build config map from env for only the keys declared in
        // factory.getConfigProperties(). Mask SECRET type values in logs.
    }
}
```

---

## Step 6: Built-in gateway providers

For each of the four gateways (Axis, PayTM, PhonePe, PayU), create two classes
in `org.egov.payment.service.gateways.{name}`:

### `{Name}GatewayProviderFactory`

Implements `GatewayProviderFactory`. Instantiated by `ServiceLoader` — **no Spring
annotations, no constructor injection**.

Read the corresponding `*Gateway` class in `$PG_SERVICE_PATH` carefully. Every
`@Value("${some.key}")` field you find becomes a `GatewayProviderConfig` entry.

```java
public class AxisGatewayProviderFactory implements GatewayProviderFactory {

    @Override public String getGatewayId() { return "axis"; }
    @Override public String getDisplayName() { return "Axis Bank"; }
    @Override public String getVersion() { return "1.0.0"; }

    @Override
    public List<GatewayProviderConfig> getConfigProperties() {
        // One entry per @Value key found in the existing AxisGateway.
        // Read $PG_SERVICE_PATH/src/.../gateways/AxisGateway.java NOW
        // and enumerate every single property key before writing this list.
        // Do not guess — read the actual code.
        return List.of(
            new GatewayProviderConfig(
                "axis.merchant.id",
                "Axis merchant identifier",
                GatewayProviderConfig.Type.STRING, true, null),
            // ... all others found in AxisGateway
        );
    }

    @Override
    public GatewayProvider create(Map<String, String> config) {
        return new AxisGatewayProvider(config);
    }
}
```

### `{Name}GatewayProvider`

Implements `GatewayProvider`. Receives all config via constructor.
No Spring. No `@Value`. No `RestTemplate`.

Carry over the **exact business logic** from the existing `*Gateway` implementation:
- Redirect URI construction (checksum/hash algorithm, parameter encoding, amount
  conversion from rupees to gateway-specific unit)
- Live status fetch (HTTP call, response parsing, status mapping)
- `transactionIdKeyInResponse()`

Use `java.net.http.HttpClient` (Java 11+) for all HTTP calls. Do not introduce
Spring's `RestTemplate` or `WebClient` into this class.

```java
public class AxisGatewayProvider implements GatewayProvider {

    private final Map<String, String> config;
    private final HttpClient httpClient;

    public AxisGatewayProvider(Map<String, String> config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {
        // Carry over logic from AxisGateway.generateRedirectURI exactly.
        // Read the source before writing this.
    }

    @Override
    public TransactionStatus fetchStatus(Transaction transaction,
                                          Map<String, String> params) {
        // Carry over logic from AxisGateway.fetchStatus exactly.
    }

    @Override
    public String transactionIdKeyInResponse() {
        // Carry over from AxisGateway.
    }
}
```

Repeat for `PaytmGatewayProvider(Factory)`, `PhonePeGatewayProvider(Factory)`,
`PayuGatewayProvider(Factory)`.

Register all four in:
```
src/main/resources/META-INF/services/org.egov.gateway.spi.GatewayProviderFactory
```
```
org.egov.payment.service.gateways.axis.AxisGatewayProviderFactory
org.egov.payment.service.gateways.paytm.PaytmGatewayProviderFactory
org.egov.payment.service.gateways.phonepe.PhonePeGatewayProviderFactory
org.egov.payment.service.gateways.payu.PayuGatewayProviderFactory
```

---

## Step 7: Application services

Reimplement these services in `org.egov.payment.service`, carrying over logic
from the corresponding classes in `$PG_SERVICE_PATH` but adapted to use the
SPI registry instead of Spring bean discovery.

### `GatewayService`

```java
@Service
public class GatewayService {

    private final GatewayProviderRegistry registry;

    /** Initiates a transaction — calls provider.generateRedirectURI */
    public Map<String, String> initiateTxn(Transaction transaction) {
        GatewayProviderFactory factory = registry.getFactory(
            transaction.getTxnGateway());
        GatewayProvider provider = factory.create(
            registry.resolvedConfigFor(transaction.getTxnGateway()));
        URI redirectUri = provider.generateRedirectURI(transaction);
        return Map.of("redirectUrl", redirectUri.toString());
    }

    /** Fetches live status — calls provider.fetchStatus */
    public TransactionStatus getLiveStatus(Transaction transaction,
                                            Map<String, String> params) {
        GatewayProviderFactory factory = registry.getFactory(
            transaction.getTxnGateway());
        GatewayProvider provider = factory.create(
            registry.resolvedConfigFor(transaction.getTxnGateway()));
        return provider.fetchStatus(transaction, params);
    }

    public boolean isActive(String gatewayName) {
        return registry.isActive(gatewayName);
    }
}
```

### `TransactionService`

Carry over the full transaction lifecycle from `$PG_SERVICE_PATH`:
- `create()` — validate → enrich → initiateTxn → persist → publish
- `update()` — validate → getLiveStatus → registerPayment if success → persist
- `skipGateway()` — zero-amount path, mark SUCCESS immediately

Wire in `TransactionValidator`, `EnrichmentService`, `PaymentsService`,
`TransactionRepository`, `GatewayService` exactly as before.

### `EnrichmentService`

Carry over completely. IdGen integration for `txnId` generation using template
code `TxnID`. Callback URL construction. PENDING status on create.

### `TransactionValidator`

Carry over completely. `validateCreateTxn` and `validateUpdateTxn`.
Preserve `CustomException` usage from the `tracer` library.

### `PaymentsService`

Carry over completely. `validatePayment` (pre-validation against Billing service)
and `registerPayment` (post-success payment registration).

---

## Step 8: DIGIT service clients

Carry over all four clients from `$PG_SERVICE_PATH/src/main/java/org/egov/pg/clients/`
into `org.egov.payment.clients`:

- `BillingServiceClient` — `billing/v3/payments`
- `IdGenClient` — `idgen/v1/generate`
- `IndividualClient` — `individual/v1`
- `BankAccountRegistryClient` — `registry/v1/schema/bankAccount/data/_search`

Use `RestTemplate` for these — they are Spring-managed application-level clients,
not part of the SPI layer. Preserve all request/response model classes.

---

## Step 9: REST layer

Reimplement in `org.egov.payment.web.controllers`:

- `POST /transaction/v3/_create` → `TransactionService.create()`
- `PUT /transaction/v3/_update` → `TransactionService.update()`
- `POST /transaction/v3/_search` → `TransactionRepository.searchTransactions()`

Preserve required headers: `X-Tenant-ID`, `X-Client-ID`.
Preserve response envelope format from `$PG_SERVICE_PATH` — DIGIT clients
depend on this shape.

---

## Step 10: Messaging

Carry over the broker abstraction from `$PG_SERVICE_PATH`:
- `Producer` interface
- `KafkaMessageProducer`, `RedisMessageProducer`, `NoOpMessageProducer`
- Resolved at startup based on `message.broker.enabled` and `message.broker.type`
- Topics: `create-pg-txn` and `update-pg-txn`
- Default: `message.broker.enabled=false` → `NoOpMessageProducer`

---

## Step 11: Reconciliation jobs

Carry over both Quartz jobs from `$PG_SERVICE_PATH/src/main/java/org/egov/pg/service/jobs/`:

- `EarlyReconciliationJob` — every 15 min (configurable via
  `pg.earlyReconcileJobRunInterval.mins`); reconciles PENDING txns 15–30 min old
- `DailyReconciliationJob` — daily; reconciles all PENDING txns older than 30 min

Both call `GatewayService.getLiveStatus` then `PaymentsService.registerPayment`
on success. Same flow as the normal update path.

---

## Step 12: `application.properties`

Carry over all config keys from `$PG_SERVICE_PATH/src/main/resources/application.properties`.
Change:

```properties
server.port=8080
server.servlet.context-path=/payment-gateway
```

Keep all of the following key groups exactly:
- `{gateway}.active` flags for all four gateways
- All gateway-specific config keys (merchant IDs, secrets, URLs, salts)
- DIGIT service URLs (billing, idgen, individual, registry)
- DB config (`db.host`, `db.port`, `db.name`, both JDBC URLs)
- Flyway config
- Quartz config
- Broker config (`message.broker.enabled`, `message.broker.type`)
- `pg.is.user.create.enabled`
- `egov.pg.reconciliation.system.user.uuid`

---

## Step 13: `CLAUDE.md`

Write a `CLAUDE.md` for the new service at the repo root. Include:

1. Build and run commands (same as pg-service but with new port/path)
2. Architecture overview — lead with the SPI pattern, explain the four layers:
    - `GatewayProviderFactory` (SPI, no Spring)
    - `GatewayProvider` (SPI, no Spring)
    - `GatewayProviderRegistry` (Spring component, owns ServiceLoader)
    - `GatewayService` (Spring service, calls registry)
3. How to add a new gateway (step by step: implement two classes + add META-INF line)
4. Transaction lifecycle (create + update flows)
5. DB tables and Flyway
6. Reconciliation jobs
7. Messaging
8. Key config properties table (same format as pg-service CLAUDE.md)

---

## Step 14: Tests

Create the following test classes:

### SPI layer tests
- `GatewayProviderRegistryTest`
    - `shouldDiscoverAllFourBuiltInFactories`
    - `shouldFailFastOnDuplicateGatewayId`
    - `shouldFailFastWhenActiveGatewayHasNoFactory`
    - `shouldFailFastWhenRequiredConfigMissing` — error message names the missing key
    - `shouldNotInitInactiveFactories`
    - `shouldLogStartupSummary`
- `MetaInfServicesFileTest`
    - `shouldContainAllFourFactoryClassNames`
    - `shouldLoadAllRegisteredClassesViaServiceLoader`
    - `allRegisteredClassesShouldBeInstantiable`

### Per-gateway tests (one class per gateway)
- `AxisGatewayProviderFactoryTest`
    - `shouldDeclareAllConfigPropertiesFoundInOriginalGateway`
    - `shouldCreateProviderFromValidConfig`
- `AxisGatewayProviderTest`
    - `shouldGenerateRedirectUriWithCorrectChecksum`
    - `shouldConvertRupeesToPaiseInRedirectUri`
    - `shouldMapGatewayResponseToTransactionStatus`
    - `shouldThrowGatewayExceptionOnConnectivityError`

  Repeat for Paytm, PhonePe, PayU.

### Application service tests
- `GatewayServiceTest`
    - `shouldCallProviderGenerateRedirectUri`
    - `shouldCallProviderFetchStatus`
    - `shouldReturnActiveStateFromRegistry`
- `TransactionServiceTest`
    - Carry over existing tests from `$PG_SERVICE_PATH/src/test/` — adapt imports only

---

## Step 15: `DECISIONS.md`

Create at the repo root. Document:

1. **Module structure** — single or multi-module, and why
2. **HttpClient choice** — `java.net.http.HttpClient` used in gateway providers;
   `RestTemplate` kept for DIGIT service clients (Spring-managed, not SPI)
3. **Config resolution** — `Environment` injected into `GatewayProviderRegistry`
   to read `{id}.active` and provider-specific keys
4. **What is deferred to Phase 2**:
    - External JAR loading from a `providers/` directory on disk
    - Per-tenant provider selection via DB config
    - Admin API to list/activate/deactivate providers at runtime
5. **Differences from pg-service** — list every deliberate change made during
   the clean reimplementation and why

---

## Hard constraints

- **Zero Spring annotations in `org.egov.gateway.spi`**. `GatewayProviderFactory`,
  `GatewayProvider`, `GatewayProviderConfig`, `GatewayException` must compile
  without Spring on the classpath. Verify this.

- **No field renaming in domain models**. `Transaction`, `TransactionStatus` field
  names map directly to DB columns. Any rename breaks the `TransactionRowMapper`.

- **Preserve the `{name}.active` config contract**. Operators running pg-service
  today must be able to switch to payment-gateway by changing only `server.port`
  and `server.servlet.context-path` — all other config keys stay the same.

- **Carry over gateway logic exactly**. The checksum/hash algorithms, amount
  conversion, and response parsing for each gateway are business-critical. Do not
  simplify or rewrite them — read the source and port faithfully.

- **Fail fast on misconfiguration**. Any missing required property or unresolvable
  factory must throw at startup with a human-readable message naming the exact
  problem. No silent fallbacks. No `NullPointerException` at request time.

- **All tests must pass** before considering any step complete. Run `mvn test`
  after completing Steps 6, 7, and 14.

- **Do not delete or modify `$PG_SERVICE_PATH`**. Read it; never write to it.

---

## Definition of done

- [ ] `mvn clean package` succeeds with zero errors
- [ ] `mvn test` is fully green
- [ ] Service starts on port 8080 at `/payment-gateway`
- [ ] All four gateway providers discovered at startup — confirmed in startup log
- [ ] Startup fails with clear error if a required config key is missing
- [ ] `POST /payment-gateway/transaction/v3/_create` returns a redirectUrl
- [ ] `PUT /payment-gateway/transaction/v3/_update` updates transaction status
- [ ] `READING-NOTES.md` exists and documents all findings from pg-service
- [ ] `DECISIONS.md` exists and is complete
- [ ] `CLAUDE.md` written for the new service
- [ ] `META-INF/services` file lists all four factories, all loadable
- [ ] Zero Spring annotations in `org.egov.gateway.spi` package
- [ ] DB schema identical to pg-service (same Flyway migrations)