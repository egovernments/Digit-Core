# Architectural Decisions — payment-gateway

This document records significant design choices made during the creation of this service
and the rationale behind each one.

---

## 1. Single-module layout (SPI + application in the same JAR)

**Decision:** The `org.egov.gateway.spi` contract and the `org.egov.payment` application code
live in the same Maven module / JAR.

**Rationale:** A separate SPI module would require a multi-module Maven parent, additional CI
configuration, and version coordination. Since no external team currently needs to publish a
gateway plugin independently, a single module keeps the build simple. The package boundary
(`org.egov.gateway.spi` vs `org.egov.payment`) is still enforced by convention: SPI classes
carry zero Spring annotations and depend only on the `Transaction` model.

**Trade-off accepted:** If an external gateway provider ever needs to publish a plugin JAR, the
SPI package must be extracted into a separate `payment-gateway-spi` artifact. That refactor is
deferred to Phase 2.

---

## 2. Java ServiceLoader for gateway discovery (not Spring @Component scanning)

**Decision:** `GatewayProviderFactory` implementations are discovered via
`java.util.ServiceLoader` at startup, driven by
`META-INF/services/org.egov.gateway.spi.GatewayProviderFactory`.

**Rationale:**
- ServiceLoader is a JDK standard with no framework dependency.
- Gateway factories carry zero Spring annotations, making them trivially unit-testable without
  starting a Spring context.
- Adding a new gateway only requires creating the factory class and adding one line to the
  services file — no changes to any Spring configuration class.
- The existing `pg-service` used Spring `@Component` scanning, which couples gateway
  implementations to Spring's lifecycle. The SPI approach removes that coupling.

---

## 3. `java.net.http.HttpClient` for gateway providers; Spring `RestClient` for DIGIT clients

**Decision:** All HTTP calls inside gateway providers (`AxisGatewayProvider`, etc.) use
`java.net.http.HttpClient` (JDK 11+). Calls to internal DIGIT services (billing, idgen,
individual, registry) use Spring's `RestClient`.

**Rationale:**
- Gateway providers must not depend on Spring to remain SPI-compatible.
- `java.net.http.HttpClient` is a JDK built-in that requires no extra dependency.
- DIGIT service clients are Spring `@Component` beans that benefit from Spring's
  `RestClient` convenience (interceptors, message converters, base-URL configuration).

---

## 4. Java 17 `HexFormat` replaces `javax.xml.bind.DatatypeConverter`

**Decision:** `PhonepeUtils.buildHash()` uses `HexFormat.of().withUpperCase().formatHex(hash)`
instead of `DatatypeConverter.printHexBinary()`.

**Rationale:**
- `javax.xml.bind` was removed from the JDK in Java 11. In Jakarta EE 9+ it is
  `jakarta.xml.bind`, but adding that dependency solely for hex encoding is wasteful.
- `java.util.HexFormat` (Java 17) is the idiomatic replacement and produces identical output
  (uppercase hex string) without any extra dependency.
- This removes the `jakarta.xml.bind-api` transitive dependency from the classpath entirely.

---

## 5. Config-driven gateway activation with fail-fast startup

**Decision:** Each gateway is activated by setting `<id>.active=true` in
`application.properties`. If a gateway is active but any required config key is missing,
`GatewayProviderRegistry` throws `IllegalStateException` at `@PostConstruct` time
with a clear list of missing keys.

**Rationale:**
- Fail-fast behaviour surfaces misconfiguration immediately at startup rather than at
  runtime when the first payment is attempted.
- All required keys are declared in `GatewayProviderFactory.getConfigProperties()`,
  so there is a single source of truth for what each gateway needs.
- Default values are supported; only keys with no default and no value trigger the error.

---

## 6. Amount handling: rupees in DB, paise on the wire

**Decision:** `Transaction.txnAmount` is stored and communicated to callers in rupees (e.g.
`"100.00"`). Gateway providers receive amounts in paise (integer, e.g. `10000`) when calling
the payment network.

**Rationale:** Preserved verbatim from `pg-service` to maintain backward compatibility with
existing DB data and billing-service contracts.

---

## 7. Port 8080 / context-path `/payment-gateway`

**Decision:** The service listens on port `8080` with context-path `/payment-gateway`.
The original `pg-service` used port `9000` / `/pg-service`.

**Rationale:** Port 8080 is the DIGIT platform standard for containerised microservices.
Renaming the context-path to `/payment-gateway` aligns it with the artifact ID and avoids
confusion with the legacy `pg-service` during a side-by-side migration.

---

## 8. Quartz scheduler name changed to `payment-gateway-quartz-scheduler`

**Decision:** `QuartzConfig` sets `schedulerName = "payment-gateway-quartz-scheduler"`.
The original value in `pg-service` was `pg-service-quartz-scheduler`.

**Rationale:** Quartz persists the scheduler name in the `eg_pg_qrtz_*` tables. If both
services were ever run against the same DB schema simultaneously, distinct scheduler names
prevent job-stealing conflicts.

---

## 9. Phase 2 deferrals

The following features from `pg-service` are present in this service but have not been
re-verified end-to-end and may require further work before production use:

- **Daily reconciliation** — logic is preserved verbatim but Quartz JDBC store setup
  requires a live PostgreSQL instance.
- **Redis Producer** — `RedisProducer` is implemented but Redis connection settings
  must be provided via `spring.data.redis.*` properties.
- **Individual / Registry clients** — present but the API contracts may drift between
  DIGIT versions.
- **External SPI module** — if a third party needs to publish a gateway plugin, the SPI
  package must be extracted into `payment-gateway-spi`.

---

## 10. Differences from `pg-service`

| Aspect | pg-service | payment-gateway |
|---|---|---|
| Port | 9000 | 8080 |
| Context path | `/pg-service` | `/payment-gateway` |
| Gateway discovery | Spring `@Component` | Java `ServiceLoader` |
| Spring in gateway code | Yes | No |
| HTTP client in gateways | `RestTemplate` | `java.net.http.HttpClient` |
| Hex encoding (PhonePe) | `DatatypeConverter` | `java.util.HexFormat` |
| Spring Boot version | (varies) | 3.2.2 |
| Java version | (varies) | 17 |
| PayU default active | true | false |
| Quartz scheduler name | `pg-service-quartz-scheduler` | `payment-gateway-quartz-scheduler` |
