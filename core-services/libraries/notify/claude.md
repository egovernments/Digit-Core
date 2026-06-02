
# CLAUDE.md — notify-service

## What this project is

A pluggable notification microservice. Inspired by Keycloak's SPI architecture.
The service dispatches notifications over multiple channels (SMS, email, WhatsApp,
push). Each channel provider is a separately deployed fat jar loaded at runtime —
not a submodule of this repo.

---

## Module structure


notify-service/          ← this repo
├── notify-spi/          ← thin contract jar, published to Maven registry
└── notify-app/          ← Spring Boot 4 fat jar, the runnable service

Provider jars (e.g. notify-provider-twilio) live in **separate repos entirely**.
They are dropped into `/providers` at deploy time and loaded at startup via
`URLClassLoader` + `ServiceLoader`.

---

## Module boundary rules — NEVER violate these

### notify-spi
- NO Spring dependencies of any kind (no @Component, no @Service, no spring-*)
- NO Jakarta EE persistence (no @Entity, no JPA)
- NO HTTP clients
- ONLY allowed dependencies: slf4j-api, jakarta.annotation-api, jspecify
- Contains ONLY: interfaces, records, enums
- This jar must be publishable standalone to Maven Central

### notify-app
- Contains ALL Spring Boot code, JPA entities, REST controllers, service logic
- Depends on notify-spi (compile scope)
- Is the ONLY deployable artifact in this repo

### Provider jars (separate repos, not here)
- Depend on notify-spi as `provided` scope
- Shade all their own dependencies (fat jar)
- NEVER bundled inside notify-app
- Registered at runtime via META-INF/services

---

## Technology stack — use exact versions, do not substitute

| Technology | Version |
|---|---|
| Java | 25 (LTS) |
| Spring Boot | 4.0.1 |
| Spring Framework | 7.0 |
| Spring Data | 2025.1 |
| Hibernate | 7.1 |
| Flyway | 11.x (via Spring Boot BOM) |
| PostgreSQL driver | via Spring Boot BOM |
| HikariCP | 7.0 (via Spring Boot BOM) |
| springdoc-openapi | latest 2.x compatible with Spring Boot 4 |
| JSpecify | 1.0.0 |
| ulid-creator | latest stable |
| json-path (Jayway) | latest stable 2.x |
| mustache.java | latest stable 0.9.x |
| MapStruct | latest stable 1.6.x |

---

## Java 25 conventions — always prefer these patterns

- Use **records** for all immutable value objects, DTOs, and API models
- Use **virtual threads** for all concurrent dispatch:
  `Executors.newVirtualThreadPerTaskExecutor()`
- Use **StructuredTaskScope** for fan-out concurrency (channel dispatch)
- Use **ScopedValue** for request-scoped context (tenantId, requestId) —
  never use ThreadLocal
- Use **@NullMarked** at package level + **@Nullable** from JSpecify on
  nullable params/fields
- Enable preview features: `--enable-preview` in compiler and surefire plugins
- Do NOT use Lombok — use records or explicit accessors
- Do NOT use RestTemplate — use RestClient for any outbound HTTP in notify-app

---

## Package root

org.digit.notify.spi.*      ← everything in notify-spi
org.digit.notify.app.*      ← everything in notify-app

Sub-packages in notify-app:

org.digit.notify.app.domain.entity      ← JPA entities
org.digit.notify.app.domain.repository  ← Spring Data repositories
org.digit.notify.app.plugin             ← ProviderPluginLoader
org.digit.notify.app.template           ← TemplateRenderer
org.digit.notify.app.dispatch           ← DispatchEngine, ProviderMappingResolver
org.digit.notify.app.service            ← NotificationService
org.digit.notify.app.controller         ← REST controllers
org.digit.notify.app.controller.dto     ← request/response DTOs
org.digit.notify.app.controller.mapper  ← MapStruct mappers
org.digit.notify.app.model              ← shared model records (NotifyRequest etc)
org.digit.notify.app.exception          ← typed exceptions
---

## Database and migrations

- Database: PostgreSQL 16
- Migration tool: **Flyway only** (no Liquibase)
- Migration files location: `notify-app/src/main/resources/db/migration/`
- Naming convention: `V{n}__{description}.sql` e.g. `V1__initial_schema.sql`
- Never use `ddl-auto: create` or `ddl-auto: update` — always `validate`
- Flyway runs automatically on startup via Spring Boot autoconfiguration

---

## OpenAPI spec

The service implements this spec exactly:
https://raw.githubusercontent.com/digitnxt/digit-specs/refs/heads/notify/v3.0.0/notify.yaml

All endpoint paths, request/response schemas, status codes, and headers must
match the spec. Do not invent endpoints or fields not in the spec.

---

## Key domain concepts

**NotificationConfig** — stores a template and its per-channel configs (body,
subject, title as locale maps, payloadBindings as JSONPath maps). Scoped to
a tenantId + templateCode. One active config per tenant+templateCode at a time.

**ProviderMapping** — maps a channel (+ optional country) to an ordered list of
provider names. First = primary, rest = fallbacks. Scoped to tenantId.
Unique on (tenantId, channel, country) where NULL country = global default.

**Provider** — registry of known providers. Auto-seeded when a jar loads at
startup. isActive can be toggled via API without restarting.

**NotificationLog** — one row per /notify call. Carries a ULID-based
notificationId (format: "ntf_<ulid>").

**NotificationAttempt** — one row per provider call attempt within a
notification, including fallbacks. This is the full audit trail.

---

## Dispatch flow summary





POST /notify
→ resolve NotificationConfig (tenantId + templateCode + isActive=true)
→ fan out to all 4 channels concurrently (StructuredTaskScope, virtual threads)
per channel:
→ if disabled: SKIPPED
→ render template (JSONPath bindings + Mustache)
→ resolve provider list (ProviderMappingResolver: country → global fallback)
→ attempt providers in order, fallback on failure
→ log each attempt (NotificationAttempt row)
→ persist NotificationLog
→ return 202 NotifyResponse




---

## Build commands

```bash
# Build everything from root
mvn clean install -DskipTests

# Build and test a single module
mvn test -pl notify-app

# Run a specific test class
mvn test -pl notify-app -Dtest=DispatchEngineTest

# Run the app locally (needs PostgreSQL running)
mvn spring-boot:run -pl notify-app -Dspring-boot.run.profiles=local

# Start local dependencies
docker compose up postgres -d

# Full stack
docker compose up --build
```

---

## Verification checks (run after any structural change)

```bash
# SPI jar must contain zero Spring classes
jar tf notify-spi/target/notify-spi-*.jar | grep -i spring  # must be empty

# notify-spi must be bundled inside the app fat jar
jar tf notify-app/target/notify-app-*.jar | grep "BOOT-INF/lib/notify-spi"

# Flyway must be bundled
jar tf notify-app/target/notify-app-*.jar | grep "flyway"
```

---

## What is NOT in this repo

- Provider implementations (notify-provider-twilio, notify-provider-sendgrid etc)
  → these are separate repos
- Any vendor SDK (Twilio, SendGrid, Kaleyra etc) → belong in provider repos only
- Any HTTP client calls to external services → notify-app never calls providers
  directly, it only loads and invokes them via the SPI interface

---

## Current build status

Track which prompts have been executed:

- [x] Prompt 1 — Maven scaffold
- [x] Prompt 2 — SPI module
- [x] Prompt 3 — JPA entities + Flyway migrations
- [x] Prompt 4 — ProviderPluginLoader
- [x] Prompt 5 — TemplateRenderer
- [x] Prompt 6 — DispatchEngine
- [x] Prompt 7 — NotificationService
- [x] Prompt 8 — REST controllers
- [x] Prompt 9 — Docker + assembly
- [x] Prompt 10 — Provider template repo (separate)

Update this checklist as prompts complete.

---

## If Claude Code gets confused

1. Re-read this file top to bottom before making any change
2. The module boundary rules section is absolute — if a change would violate
   them, do not make the change, ask instead
3. When in doubt about a Java version or Spring Boot version, check the
   Technology stack table above — do not infer from training data