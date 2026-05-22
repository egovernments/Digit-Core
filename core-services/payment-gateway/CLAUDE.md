# payment-gateway — Developer Guide for Claude Code

## Build and Run

```bash
# Compile and package (skip tests)
mvn clean package -DskipTests -f core-services/payment-gateway/pom.xml

# Compile and run all tests
mvn test -f core-services/payment-gateway/pom.xml

# Run the service (requires PostgreSQL + application.properties configured)
java -jar target/payment-gateway-1.0.0-SNAPSHOT.jar
```

## Key properties to configure before starting

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/egov_pg
spring.datasource.username=postgres
spring.datasource.password=postgres

# Activate a gateway (example: Axis)
axis.active=true
axis.merchant.id=YOUR_MID
axis.merchant.secret.key=YOUR_HEX_SECRET   # 64-char hex (32 bytes)
axis.merchant.user=YOUR_AMA_USER
axis.merchant.pwd=YOUR_AMA_PWD
axis.merchant.access.code=YOUR_ACCESS_CODE
```

All other axis.* keys have defaults (see `AxisGatewayProviderFactory`). The service will
refuse to start if an active gateway is missing a required key — the error message lists
exactly which keys are missing.

---

## Architecture

### Package layout

```
org.egov.gateway.spi          SPI contract — ZERO Spring annotations
  GatewayProviderFactory      ServiceLoader entry point; declares config, creates providers
  GatewayProvider             Per-request operations: redirect URI + status fetch
  GatewayProviderConfig       Immutable config descriptor with optional default value
  GatewayException            Runtime exception with typed ErrorType enum

org.egov.payment              Spring Boot application
  PaymentGatewayApplication   Main class (@SpringBootApplication)
  config/                     Spring @Configuration beans (AppProperties, MainConfig, etc.)
  models/                     Domain models (Transaction, User, TaxAndPayment, TransactionDump)
  repository/                 JdbcTemplate-based persistence (no ORM)
  service/
    registry/                 GatewayProviderRegistry — ServiceLoader discovery + activation
    GatewayService            Spring @Service adapter over the registry
    TransactionService        Core transaction create/update/fetch business logic
    EnrichmentService         ID generation, audit details, amount formatting
    PaymentsService           Billing-service payment create/update calls
    UserService               Individual/user lookup and creation
    jobs/                     Quartz reconciliation jobs (early + daily)
    gateways/
      axis/                   Axis Bank — HMAC-SHA256, vpc_* params
      paytm/                  Paytm — proprietary checksum library
      phonepe/                PhonePe — Base64 JSON, SHA-256 X-VERIFY (Java 17 HexFormat)
      payu/                   PayU — SHA-512 hash, form-urlencoded POST
  web/controllers/            REST controllers
  clients/                    RestClient-based DIGIT service clients
  messaging/                  Kafka / Redis / NoOp producer abstraction
```

### Gateway plugin lifecycle (ServiceLoader)

1. At startup `GatewayProviderRegistry.initialize()` calls
   `ServiceLoader.load(GatewayProviderFactory.class)`.
2. Every entry in
   `src/main/resources/META-INF/services/org.egov.gateway.spi.GatewayProviderFactory`
   is instantiated.
3. For each discovered factory, the registry checks `<id>.active` in the Spring
   `Environment`. If `true`, it resolves all config keys, validates required ones, and
   calls `factory.init(config)`.
4. At request time `GatewayService` calls `factory.create(config)` to obtain a
   `GatewayProvider` and delegates `generateRedirectURI` or `fetchStatus` to it.
5. At shutdown `@PreDestroy` calls `factory.close()` on every active factory.

### Transaction lifecycle

```
POST /payment-gateway/transaction/v3/_create
  → TransactionValidator.validateTransactionForCreate()
  → EnrichmentService.enrichCreateTransaction()   (generates txnId via idgen)
  → GatewayService.initiateTxn()                  (calls gateway provider)
  → TransactionRepository.saveTransaction()
  → return redirect URI

POST /payment-gateway/transaction/v3/_update  (gateway callback)
  → TransactionService.updateTransaction()
  → GatewayService.getLiveStatus()               (fetch authoritative status)
  → TransactionRepository.updateTransaction()
  → PaymentsService.registerPayment()             (notify billing service)

GET /payment-gateway/transaction/v3/_search
  → TransactionRepository.fetchTransactions()

GET /payment-gateway/gateway/v3/_search
  → GatewayService.getActiveGateways()
```

---

## How to add a new gateway

1. Create `org.egov.payment.service.gateways.<name>/<Name>GatewayProvider.java`
   - Implement `GatewayProvider` (no Spring annotations)
   - Use `java.net.http.HttpClient` for HTTP calls
   - Constructor takes `Map<String,String> config`

2. Create `<Name>GatewayProviderFactory.java` (public, no Spring annotations)
   - Implement `GatewayProviderFactory`
   - `getGatewayId()` returns a stable lowercase ID (e.g. `"razorpay"`)
   - `getConfigProperties()` lists all required + optional keys with defaults
   - `create(config)` returns `new <Name>GatewayProvider(config)`

3. Register the factory:
   Add one line to
   `src/main/resources/META-INF/services/org.egov.gateway.spi.GatewayProviderFactory`:
   ```
   org.egov.payment.service.gateways.<name>.<Name>GatewayProviderFactory
   ```

4. Activate in `application.properties`:
   ```properties
   <name>.active=true
   <name>.<required-key>=value
   ```

5. Write tests in `src/test/java/.../gateways/<name>/`:
   - `<Name>GatewayProviderFactoryTest` — verify gateway ID, config keys, `create()`
   - `<Name>UtilsTest` (if applicable) — hash/checksum logic

No changes to any existing Spring configuration class are needed.

---

## Database

Flyway runs three migrations on startup:
- `V20180607185601__eg_pg_ddl.sql` — `eg_pg_transactions` and `eg_pg_transactions_dump`
- `V20180613110701__eg_pg_quartz_ddl.sql` — Quartz tables (prefix `eg_pg_qrtz_`)
- `V20190326220501__alter_eg_pg_transactions.sql` — adds `consumer_code` column

Flyway location: `db/migration/main`

---

## Reconciliation jobs

Two Quartz jobs run on a JDBC job store (same PostgreSQL schema):

- **EarlyReconciliationJob** — runs every N minutes (configurable via
  `egov.pg.reconcile.job.run.every.hours/minutes`). Fetches PENDING transactions
  created more than N minutes ago and checks their live status.
- **DailyReconciliationJob** — runs at midnight and noon (`0 0 0,12 * * ?`).
  Fetches all PENDING transactions from epoch 0 to now, checks live status.

---

## Messaging

The `Producer` interface abstracts three implementations:
- `KafkaProducer` — sends to Kafka topic (used when `message.broker.enabled=true` and
  `message.broker.type=kafka`)
- `RedisProducer` — publishes to Redis channel (type `redis`)
- `NoOpMessageProducer` — no-op (default when broker disabled)

`MessagingConfig` resolves the correct bean at startup via `@ConditionalOnProperty`.
