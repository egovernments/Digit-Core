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
- [ ] Prompt 2 — SPI module
- [ ] Prompt 3 — JPA entities + Flyway migrations
- [ ] Prompt 4 — ProviderPluginLoader
- [ ] Prompt 5 — TemplateRenderer
- [ ] Prompt 6 — DispatchEngine
- [ ] Prompt 7 — NotificationService
- [ ] Prompt 8 — REST controllers
- [ ] Prompt 9 — Docker + assembly
- [ ] Prompt 10 — Provider template repo (separate)

Update this checklist as prompts complete.

---

## If Claude Code gets confused

1. Re-read this file top to bottom before making any change
2. The module boundary rules section is absolute — if a change would violate
   them, do not make the change, ask instead
3. When in doubt about a Java version or Spring Boot version, check the
   Technology stack table above — do not infer from training data

Where to place it
Put it at the repo root as CLAUDE.md. Claude Code reads it automatically on every session start. You can also tell Claude Code explicitly at the start of a session: "read CLAUDE.md before doing anything."
The checklist at the bottom is especially useful — update it as you complete each prompt so Claude Code knows the current state without you having to explain it every session.You said: How should I give all this prompts?How should I give all this prompts? SHould I create seaparate file for each?14:57Claude responded: Good question.Good question. Here's the practical answer.
Recommended approach — one file, sectioned
Create a single file called PROMPTS.md at the repo root. Keep all 10 prompts in it as clearly separated sections. Do not create separate files — you'll end up hunting across files to track what's done and what's next.
notify-service/
├── CLAUDE.md        ← project context, conventions, architecture
├── PROMPTS.md       ← all 10 prompts in sequence
└── ...

How to actually execute each prompt in Claude Code
You don't paste the whole prompt file. For each task you do this:
Step 1 — Start a fresh Claude Code session (or /clear to reset context)
Step 2 — Say:
Read CLAUDE.md, then execute Prompt 3 from PROMPTS.md
That's it. Claude Code reads the project context from CLAUDE.md first, then finds and executes the right prompt.
Step 3 — After it finishes, verify using the steps at the bottom of the prompt. If something is wrong, fix it in the same session.
Step 4 — Update the checklist in CLAUDE.md:
- [x] Prompt 3 — JPA entities + Flyway migrations
Step 5 — Commit before moving to the next prompt.

Why commit between prompts
Each prompt builds on the previous. If Prompt 5 breaks something from Prompt 4, you want to be able to git diff or git reset cleanly rather than untangle two prompts worth of changes at once. Keep commits small and prompt-scoped:
bashgit add .
git commit -m "feat: prompt 3 — JPA entities and Flyway migrations"

When a prompt fails halfway
Don't start over. Tell Claude Code in the same session:
The test DispatchEngineTest is failing with this error: [paste error]
Fix it without changing anything outside the dispatch package.
The constraint at the end ("without changing anything outside...") is important — it stops Claude Code from refactoring unrelated things to make the test pass.

One thing to add to CLAUDE.md
Add this section so Claude Code always knows where to find the prompts:
markdown## Prompts

All task prompts are in `PROMPTS.md` at the repo root.
Execute them in order (Prompt 1 → 10).
Never execute a prompt without reading CLAUDE.md first.
Check the build status checklist above before starting any prompt
to know which one to run next.
This way the instruction to read CLAUDE.md before doing anything becomes self-reinforcing — CLAUDE.md points to PROMPTS.md, and PROMPTS.md prompts are written assuming CLAUDE.md has already been read.You said: give me the all the 10 prompts in copy paste format, currently all the test starting with < s not in the embedded textgive me the all the 10 prompts in copy paste format, currently all the test starting with < s not in the embedded text14:58Claude responded: Here are all 10 prompts in clean copy-paste format for PROMPTS.Here are all 10 prompts in clean copy-paste format for PROMPTS.md.

markdown# PROMPTS.md — notify-service

---

## Prompt 1 — Maven Multi-Module Scaffold

### Goal

Create the Maven multi-module project scaffold for `notify-service`. This is the
foundation — no application code yet, just the correct module structure, parent
POM, and dependency wiring so that `mvn clean install -DskipTests` succeeds from
the root.

### Architecture context

`notify-service` is a pluggable notification microservice modelled after Keycloak's
SPI architecture. Two modules only:

- `notify-spi` — zero-Spring contract jar. Published independently to a Maven
  registry. Third-party provider authors depend on this and nothing else.
- `notify-app` — Spring Boot 4 fat jar. Merges REST layer, service layer, and
  persistence. At runtime it scans a filesystem directory (default `/providers`)
  for provider jars, loads each via URLClassLoader + Java ServiceLoader, and
  registers discovered providers into an in-memory map.

Provider jars (e.g. `notify-provider-twilio.jar`) live in entirely separate repos.
They shade their own SDK/HTTP-client dependencies and declare `notify-spi` as
`provided` scope so the SPI classes are shared via the app classloader at runtime.

### Module structure to create

```
notify-service/
├── pom.xml
├── notify-spi/
│   ├── pom.xml
│   └── src/main/java/org/digit/notify/spi/.gitkeep
└── notify-app/
    ├── pom.xml
    └── src/main/java/org/digit/notify/app/.gitkeep
```

### Specification

#### Parent POM

```xml
org.digit.notify
notify-service
1.0.0-SNAPSHOT
pom
```

Properties:

```xml

  25
  25
  25
  UTF-8

```

- Spring Boot BOM: `4.0.1` imported in `<dependencyManagement>` via
  `spring-boot-dependencies` with scope `import`, type `pom`
- `<modules>`: `notify-spi`, `notify-app`
- Plugin management:
  - `maven-compiler-plugin` 3.13.x — Java 25, enable preview via
    `<compilerArgs><arg>--enable-preview</arg></compilerArgs>`
  - `maven-surefire-plugin` 3.x — `<argLine>--enable-preview</argLine>`
- No `<dependencies>` at parent level — only `<dependencyManagement>`

#### notify-spi/pom.xml

```xml
org.digit.notify:notify-service:1.0.0-SNAPSHOT
notify-spi
jar
```

Dependencies (NO Spring):

- `org.slf4j:slf4j-api` — `compile` (version from BOM)
- `jakarta.annotation:jakarta.annotation-api` — `provided`
- `org.jspecify:jspecify:1.0.0` — `compile`

#### notify-app/pom.xml

```xml
org.digit.notify:notify-service:1.0.0-SNAPSHOT
notify-app
jar
```

Dependencies:

- `org.digit.notify:notify-spi:1.0.0-SNAPSHOT` — `compile`
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-actuator`
- `org.postgresql:postgresql` — `runtime`
- `org.flywaydb:flyway-core` — `compile` (version from BOM, Flyway 11.x)
- `org.flywaydb:flyway-database-postgresql` — `compile` (same version as flyway-core)
- `org.springdoc:springdoc-openapi-starter-webmvc-ui` — latest 2.x compatible
  with Spring Boot 4, pin explicit version (check Maven Central at time of execution)
- `org.springframework.boot:spring-boot-starter-test` — `test`

Plugins:

- `spring-boot-maven-plugin` — configured to repackage into a fat jar
- `maven-compiler-plugin` inherited from parent

#### Stub application class

Create `notify-app/src/main/java/org/digit/notify/app/NotifyApplication.java`:

```java
package org.digit.notify.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotifyApplication.class, args);
    }
}
```

#### Minimal application.yml

Create `notify-app/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: notify-service
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/notify}
    username: ${SPRING_DATASOURCE_USERNAME:notify}
    password: ${SPRING_DATASOURCE_PASSWORD:notify}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false

notify:
  plugins:
    directory: ${NOTIFY_PLUGINS_DIR:/providers}

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Create the Flyway migration directory placeholder:
`notify-app/src/main/resources/db/migration/.gitkeep`

### Verification

Run all of the following — all must pass before moving to Prompt 2:

```bash
# 1. Full build succeeds
mvn clean install -DskipTests

# 2. SPI jar contains zero Spring classes (must return empty)
jar tf notify-spi/target/notify-spi-*.jar | grep -i spring

# 3. SPI jar exists and is valid
jar tf notify-spi/target/notify-spi-*.jar | grep "org/digit/notify/spi"

# 4. SPI jar bundled inside the app fat jar
jar tf notify-app/target/notify-app-*.jar | grep "BOOT-INF/lib/notify-spi"

# 5. Flyway bundled inside the app fat jar
jar tf notify-app/target/notify-app-*.jar | grep "flyway"
```

---

## Prompt 2 — SPI Module: Shared Contracts

### Goal

Implement the `notify-spi` module — the thin, zero-Spring contract jar that all
provider authors depend on. This is the public API surface of the entire system.
Once published it must be versioned carefully.

### Architecture context

Read CLAUDE.md for full context. Key constraint for this prompt: nothing in this
module may depend on Spring, Jakarta Persistence, or any HTTP client. The jar
must be publishable standalone. Provider jars in separate repos will declare
this as `provided` scope so the SPI classloader is shared at runtime with the
app classloader — this is what allows the app to cast loaded classes to
`NotificationChannelProvider` without a `ClassCastException`.

### Specification

#### Package

All code under `org.digit.notify.spi`

#### Enums

`Channel.java`:

```java
package org.digit.notify.spi;

public enum Channel {
    SMS, EMAIL, WHATSAPP, PUSH
}
```

`Priority.java`:

```java
package org.digit.notify.spi;

public enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}
```

`DispatchStatus.java`:

```java
package org.digit.notify.spi;

public enum DispatchStatus {
    DISPATCHED, SKIPPED, FAILED
}
```

#### Records

`ChannelMessage.java` — the rendered, ready-to-send message:

```java
package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import java.util.Map;

@NullMarked
public record ChannelMessage(
    Channel channel,
    String renderedBody,
    @Nullable String renderedSubject,
    @Nullable String renderedTitle,
    Map metadata
) {}
```

`Recipient.java`:

```java
package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Map;

@NullMarked
public record Recipient(
    @Nullable String phone,
    @Nullable String email,
    List deviceTokens,
    @Nullable String countryCode,
    Map metadata
) {}
```

`DispatchResult.java` — result of a single provider send attempt:

```java
package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record DispatchResult(
    Channel channel,
    DispatchStatus status,
    String providerName,
    @Nullable String reason
) {
    public static DispatchResult dispatched(Channel channel, String providerName) {
        return new DispatchResult(channel, DispatchStatus.DISPATCHED, providerName, null);
    }

    public static DispatchResult skipped(Channel channel, String reason) {
        return new DispatchResult(channel, DispatchStatus.SKIPPED, "none", reason);
    }

    public static DispatchResult failed(Channel channel, String providerName, String reason) {
        return new DispatchResult(channel, DispatchStatus.FAILED, providerName, reason);
    }
}
```

#### Interface

`NotificationChannelProvider.java`:

```java
package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
import java.util.Map;

@NullMarked
public interface NotificationChannelProvider {

    Channel supportedChannel();

    String providerName();

    DispatchResult send(
        ChannelMessage message,
        Recipient recipient,
        Map metadata
    );
}
```

#### package-info.java

Create `src/main/java/org/digit/notify/spi/package-info.java`:

```java
@NullMarked
package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
```

#### module-info.java

Create `src/main/java/module-info.java`:

```java
module org.digit.notify.spi {
    requires org.slf4j;
    requires static jakarta.annotation;
    requires org.jspecify;

    exports org.digit.notify.spi;

    uses org.digit.notify.spi.NotificationChannelProvider;
}
```

### Verification

```bash
# 1. Module builds cleanly
mvn clean install -pl notify-spi

# 2. No Spring classes in jar (must return empty)
jar tf notify-spi/target/notify-spi-*.jar | grep -i spring

# 3. No JPA classes in jar (must return empty)
jar tf notify-spi/target/notify-spi-*.jar | grep -i "jakarta/persistence"

# 4. All expected classes present
jar tf notify-spi/target/notify-spi-*.jar | grep "\.class"

# 5. Full build still succeeds
mvn clean install -DskipTests
```

---

## Prompt 3 — App Module: Domain Entities & JPA Repositories

### Goal

Implement all JPA entities, Spring Data repositories, and Flyway migrations for
`notify-app`. This is the persistence foundation that every other component in
the service builds on.

### Architecture context

Read CLAUDE.md for full context. Stack: Java 25, Spring Boot 4.0.1, Spring
Framework 7, Spring Data 2025.1, Hibernate 7.1, PostgreSQL 16, Flyway 11.x.
No Lombok — use Java records for value objects, plain classes with explicit
accessors for JPA entities (JPA requires mutable classes with no-arg constructors).

### Data model

#### notification_config table

Stores notification templates and per-channel configuration.

Columns:

- `id` UUID PK, auto-generated
- `tenant_id` VARCHAR NOT NULL
- `template_code` VARCHAR NOT NULL
- `is_active` BOOLEAN NOT NULL DEFAULT true
- `channels` JSONB NOT NULL — stores the entire channel config object
- `created_by` VARCHAR, `created_time` TIMESTAMP, `last_modified_by` VARCHAR,
  `last_modified_time` TIMESTAMP
- UNIQUE constraint on `(tenant_id, template_code)`
- INDEX on `tenant_id`

The `channels` JSONB structure:

```json
{
  "sms": {
    "enabled": true,
    "priority": "HIGH",
    "body": { "en": "Your OTP is {{otp}}", "default": "Your OTP is {{otp}}" },
    "payloadBindings": { "otp": "$.data.otp_code" }
  },
  "email": {
    "enabled": true,
    "priority": "MEDIUM",
    "subject": { "en": "Your OTP", "default": "Your OTP" },
    "body": { "en": "Your OTP is {{otp}}", "default": "Your OTP is {{otp}}" },
    "payloadBindings": { "otp": "$.data.otp_code" }
  },
  "whatsapp": null,
  "push": null
}
```

#### provider_mapping table

Maps a channel (plus optional country) to an ordered list of provider names.

Columns:

- `id` UUID PK
- `tenant_id` VARCHAR NOT NULL
- `channel` VARCHAR NOT NULL
- `country` VARCHAR NULLABLE (NULL = global fallback)
- `providers` JSONB NOT NULL — ordered `["twilio", "kaleyra"]`
- `created_by`, `created_time`, `last_modified_by`, `last_modified_time`
- UNIQUE constraint on `(tenant_id, channel, country)` — handle NULL country
  uniqueness at service layer since SQL NULL != NULL

#### provider table

Registry of known providers. Auto-seeded at startup by ProviderPluginLoader.

Columns:

- `id` UUID PK
- `provider_name` VARCHAR NOT NULL UNIQUE
- `channels` JSONB NOT NULL — `["SMS", "WHATSAPP"]`
- `is_active` BOOLEAN NOT NULL DEFAULT true
- `created_by`, `created_time`, `last_modified_by`, `last_modified_time`

#### notification_log table

One row per `/notify` call.

Columns:

- `id` UUID PK
- `notification_id` VARCHAR NOT NULL — ULID format `ntf_01J2K9X8Z...`, indexed
- `tenant_id` VARCHAR NOT NULL
- `template_code` VARCHAR NOT NULL
- `recipient_ref` VARCHAR — phone or email for traceability
- `created_at` TIMESTAMP NOT NULL

#### notification_attempt table

One row per provider call attempt including fallbacks.

Columns:

- `id` UUID PK
- `notification_id` VARCHAR NOT NULL — FK to notification_log.notification_id
- `channel` VARCHAR NOT NULL
- `provider_name` VARCHAR NOT NULL
- `attempt_no` INTEGER NOT NULL — 1 = primary, 2 = first fallback
- `status` VARCHAR NOT NULL — DISPATCHED, SKIPPED, FAILED
- `reason` VARCHAR NULLABLE
- `attempted_at` TIMESTAMP NOT NULL
- INDEX on `notification_id`

### Specification

#### Package structure

```
org.digit.notify.app.domain.entity
org.digit.notify.app.domain.repository
org.digit.notify.app.domain.converter
```

#### ChannelsConfig POJO (for JSONB column)

Create plain POJOs (not JPA entities, not records — needs Jackson serialisation):

```java
// org.digit.notify.app.domain.entity.config.ChannelsConfig
public class ChannelsConfig {
    private ChannelConfig sms;
    private EmailChannelConfig email;
    private ChannelConfig whatsapp;
    private PushChannelConfig push;
    // getters + setters
}

// org.digit.notify.app.domain.entity.config.ChannelConfig
public class ChannelConfig {
    private boolean enabled;
    private String priority;
    private Map body;
    private Map payloadBindings;
    // getters + setters
}

// org.digit.notify.app.domain.entity.config.EmailChannelConfig extends ChannelConfig
// adds: Map subject

// org.digit.notify.app.domain.entity.config.PushChannelConfig extends ChannelConfig
// adds: Map title
```

#### JPA AttributeConverters

Create `org.digit.notify.app.domain.converter.JsonbConverter<T>` — a generic
base `AttributeConverter` using Jackson `ObjectMapper` to serialize/deserialize
a Java type to/from a String (stored as JSONB in PostgreSQL).

Create specific converters:

- `ChannelsConfigConverter extends JsonbConverter<ChannelsConfig>`
- `JsonStringListConverter extends JsonbConverter<List<String>>`

Annotate each with `@Converter(autoApply = false)`.

#### JPA Entities

`NotificationConfigEntity`:

```java
@Entity
@Table(name = "notification_config",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "template_code"}))
public class NotificationConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "template_code", nullable = false)
    private String templateCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Convert(converter = ChannelsConfigConverter.class)
    @Column(name = "channels", columnDefinition = "jsonb", nullable = false)
    private ChannelsConfig channels;

    @Embedded
    private AuditDetail auditDetail = new AuditDetail();

    // explicit getters + setters, no Lombok
}
```

`ProviderMappingEntity`:

```java
@Entity
@Table(name = "provider_mapping")
public class ProviderMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "country")
    private String country; // nullable

    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "providers", columnDefinition = "jsonb", nullable = false)
    private List providers;

    @Embedded
    private AuditDetail auditDetail = new AuditDetail();

    // explicit getters + setters
}
```

`ProviderEntity`:

```java
@Entity
@Table(name = "provider")
public class ProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider_name", nullable = false, unique = true)
    private String providerName;

    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "channels", columnDefinition = "jsonb", nullable = false)
    private List channels;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Embedded
    private AuditDetail auditDetail = new AuditDetail();

    // explicit getters + setters
}
```

`NotificationLogEntity`:

```java
@Entity
@Table(name = "notification_log")
public class NotificationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "notification_id", nullable = false)
    private String notificationId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "template_code", nullable = false)
    private String templateCode;

    @Column(name = "recipient_ref")
    private String recipientRef;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // explicit getters + setters
}
```

`NotificationAttemptEntity`:

```java
@Entity
@Table(name = "notification_attempt")
public class NotificationAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "notification_id", nullable = false)
    private String notificationId;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "reason")
    private String reason;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    // explicit getters + setters
}
```

`AuditDetail` (embeddable):

```java
@Embeddable
public class AuditDetail {

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_time")
    private Instant createdTime;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "last_modified_time")
    private Instant lastModifiedTime;

    // explicit getters + setters
}
```

#### Spring Data Repositories

`NotificationConfigRepository`:

```java
public interface NotificationConfigRepository
    extends JpaRepository {

    Optional findByTenantIdAndTemplateCodeAndIsActiveTrue(
        String tenantId, String templateCode);

    List findByTenantId(String tenantId);

    List findByTenantIdAndIsActive(
        String tenantId, boolean isActive);

    List findByTenantIdAndTemplateCode(
        String tenantId, String templateCode);
}
```

`ProviderMappingRepository`:

```java
public interface ProviderMappingRepository
    extends JpaRepository {

    List findByTenantIdAndChannel(
        String tenantId, String channel);

    Optional findByTenantIdAndChannelAndCountry(
        String tenantId, String channel, String country);

    Optional findByTenantIdAndChannelAndCountryIsNull(
        String tenantId, String channel);
}
```

`ProviderRepository`:

```java
public interface ProviderRepository
    extends JpaRepository {

    Optional findByProviderName(String providerName);

    List findByIsActive(boolean isActive);
}
```

`NotificationLogRepository`:

```java
public interface NotificationLogRepository
    extends JpaRepository {

    Optional findByNotificationId(String notificationId);
}
```

`NotificationAttemptRepository`:

```java
public interface NotificationAttemptRepository
    extends JpaRepository {

    List findByNotificationId(String notificationId);
}
```

#### Flyway migration

Create `notify-app/src/main/resources/db/migration/V1__initial_schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS notification_config (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(255) NOT NULL,
    template_code    VARCHAR(255) NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT true,
    channels         JSONB NOT NULL,
    created_by       VARCHAR(255),
    created_time     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_time TIMESTAMP,
    CONSTRAINT uq_notification_config_tenant_template
        UNIQUE (tenant_id, template_code)
);

CREATE INDEX IF NOT EXISTS idx_notification_config_tenant
    ON notification_config (tenant_id);

CREATE TABLE IF NOT EXISTS provider_mapping (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(255) NOT NULL,
    channel          VARCHAR(50)  NOT NULL,
    country          VARCHAR(10),
    providers        JSONB NOT NULL,
    created_by       VARCHAR(255),
    created_time     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_time TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_provider_mapping_tenant_channel
    ON provider_mapping (tenant_id, channel);

CREATE TABLE IF NOT EXISTS provider (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_name    VARCHAR(255) NOT NULL UNIQUE,
    channels         JSONB NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT true,
    created_by       VARCHAR(255),
    created_time     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id VARCHAR(255) NOT NULL,
    tenant_id       VARCHAR(255) NOT NULL,
    template_code   VARCHAR(255) NOT NULL,
    recipient_ref   VARCHAR(255),
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_log_notification_id
    ON notification_log (notification_id);

CREATE TABLE IF NOT EXISTS notification_attempt (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id VARCHAR(255) NOT NULL,
    channel         VARCHAR(50)  NOT NULL,
    provider_name   VARCHAR(255) NOT NULL,
    attempt_no      INTEGER NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    reason          VARCHAR(1000),
    attempted_at    TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_attempt_notification_id
    ON notification_attempt (notification_id);
```

Delete the `.gitkeep` file from `db/migration/` after creating this file.

#### Test

Create `notify-app/src/test/java/org/digit/notify/app/domain/repository/RepositoryIntegrationTest.java`:

Use `@DataJpaTest` with Testcontainers PostgreSQL (`@Testcontainers`,
`@Container` with `PostgreSQLContainer`). Configure Flyway to run migrations
in test. Test the following:

- Save and retrieve a `NotificationConfigEntity` — assert id generated,
  `channels` JSONB round-trips correctly through the converter
- `findByTenantIdAndTemplateCodeAndIsActiveTrue` — returns present when
  isActive=true, empty when isActive=false
- Save a `ProviderMappingEntity` — assert `providers` list round-trips via
  JSONB converter
- `findByTenantIdAndChannelAndCountryIsNull` — returns correct global mapping
- Save `NotificationLogEntity` + two `NotificationAttemptEntity` rows —
  `findByNotificationId` returns both attempts

### Verification

```bash
# 1. Tests pass (requires Docker for Testcontainers)
mvn test -pl notify-app -Dtest=RepositoryIntegrationTest

# 2. Full build succeeds
mvn clean install -DskipTests
```

---

## Prompt 4 — App Module: Plugin Loader

### Goal

Implement `ProviderPluginLoader` — the Spring component that scans a directory
at startup, loads provider jars via URLClassLoader + ServiceLoader, auto-seeds
the provider registry in the database, and builds the in-memory dispatch map.

### Architecture context

Read CLAUDE.md for full context. This is the core of the pluggable architecture.
Key constraints:

- Each provider jar is loaded in its own `URLClassLoader` with the app
  classloader as parent. This ensures `notify-spi` classes (loaded by the app
  classloader) are shared — preventing `ClassCastException` when casting to
  `NotificationChannelProvider`.
- Provider jars are fat jars — they shade their own dependencies. The app knows
  nothing about Twilio, SendGrid etc.
- A bad jar must not crash startup. Errors are logged per jar and loading
  continues.
- `ProviderEntity.isActive` is set to `true` only on first insert. Subsequent
  restarts preserve whatever an operator has set via API.

### In-memory registry structure

```
Map<Channel, Map<String, NotificationChannelProvider>>

Example after loading two jars:
  SMS     → { "twilio": TwilioProvider@1a2b, "kaleyra": KaleyraProvider@3c4d }
  EMAIL   → { "sendgrid": SendGridProvider@5e6f }
  PUSH    → {}
  WHATSAPP→ {}
```

### Specification

#### Package

`org.digit.notify.app.plugin`

#### ProviderPluginLoader

```java
@Component
public class ProviderPluginLoader {

    private final Map> registry =
        new ConcurrentHashMap<>();

    private final ProviderRepository providerRepository;
    private final Path pluginsDirectory;

    public ProviderPluginLoader(
        ProviderRepository providerRepository,
        @Value("${notify.plugins.directory:/providers}") String pluginsDir
    ) {
        this.providerRepository = providerRepository;
        this.pluginsDirectory = Path.of(pluginsDir);
        // initialise empty maps for all channels
        for (Channel channel : Channel.values()) {
            registry.put(channel, new ConcurrentHashMap<>());
        }
    }

    @PostConstruct
    public void loadPlugins() { ... }

    private void loadJar(Path jarPath) { ... }

    @Transactional
    public void upsertProvider(NotificationChannelProvider provider) { ... }

    public Optional getProvider(
        Channel channel, String providerName) { ... }

    public List getProvidersOrdered(
        Channel channel, List orderedNames) { ... }
}
```

#### loadPlugins() implementation detail

Use Java 25 virtual threads with `StructuredTaskScope.ShutdownOnFailure` for
parallel jar loading:

```java
@PostConstruct
public void loadPlugins() {
    if (!Files.exists(pluginsDirectory)) {
        log.warn("Plugins directory does not exist: {}", pluginsDirectory);
        return;
    }

    List jars;
    try (var stream = Files.list(pluginsDirectory)) {
        jars = stream
            .filter(p -> p.toString().endsWith(".jar"))
            .toList();
    } catch (IOException e) {
        log.error("Failed to list plugins directory: {}", pluginsDirectory, e);
        return;
    }

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        jars.forEach(jar -> scope.fork(() -> {
            loadJar(jar);
            return null;
        }));
        scope.join();
        // do NOT call scope.throwIfFailed() — individual jar failures are
        // handled inside loadJar(), we never want to abort the entire startup
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("Plugin loading interrupted");
    }

    logStartupSummary();
}
```

#### loadJar() implementation detail

```java
private void loadJar(Path jarPath) {
    try {
        var classLoader = new URLClassLoader(
            new URL[]{ jarPath.toUri().toURL() },
            Thread.currentThread().getContextClassLoader()
        );
        var serviceLoader = ServiceLoader.load(
            NotificationChannelProvider.class, classLoader);

        int count = 0;
        for (NotificationChannelProvider provider : serviceLoader) {
            upsertProvider(provider);
            registry.get(provider.supportedChannel())
                    .put(provider.providerName(), provider);
            log.info("Registered provider: {} for channel: {}",
                provider.providerName(), provider.supportedChannel());
            count++;
        }

        if (count == 0) {
            log.warn("No providers found in jar: {}", jarPath.getFileName());
        }
    } catch (Exception e) {
        log.error("Failed to load jar: {} — {}",
            jarPath.getFileName(), e.getMessage(), e);
    }
}
```

#### upsertProvider() implementation detail

```java
@Transactional
public void upsertProvider(NotificationChannelProvider provider) {
    var existing = providerRepository.findByProviderName(provider.providerName());
    if (existing.isPresent()) {
        // update channels list only — preserve isActive
        var entity = existing.get();
        entity.setChannels(List.of(provider.supportedChannel().name()));
        entity.getAuditDetail().setLastModifiedTime(Instant.now());
        providerRepository.save(entity);
    } else {
        // insert new — isActive defaults to true
        var entity = new ProviderEntity();
        entity.setProviderName(provider.providerName());
        entity.setChannels(List.of(provider.supportedChannel().name()));
        entity.setActive(true);
        var audit = new AuditDetail();
        audit.setCreatedTime(Instant.now());
        entity.setAuditDetail(audit);
        providerRepository.save(entity);
    }
}
```

#### getProvidersOrdered() implementation detail

```java
public List getProvidersOrdered(
    Channel channel, List orderedNames
) {
    var channelMap = registry.get(channel);
    var result = new ArrayList();
    for (String name : orderedNames) {
        var provider = channelMap.get(name);
        if (provider != null) {
            result.add(provider);
        } else {
            log.warn("Provider '{}' listed in mapping for channel {} " +
                "but not found in registry — skipping", name, channel);
        }
    }
    return result;
}
```

#### logStartupSummary() implementation detail

Log at INFO level: total jars scanned, total providers registered, and per
channel the list of registered provider names. Example:

```
Plugin loader summary: 2 jars scanned, 3 providers registered
  SMS     : [twilio, kaleyra]
  EMAIL   : [sendgrid]
  WHATSAPP: []
  PUSH    : []
```

#### Unit test

Create `PluginLoaderTest` in `src/test/java/org/digit/notify/app/plugin/`:

The test must build a real in-memory jar at test time to prove ServiceLoader
discovery works end-to-end:

```java
@Test
void shouldDiscoverAndRegisterProviderFromJar() throws Exception {
    // 1. Write a pre-compiled TestProvider class to a temp jar
    //    Use a test fixture: create a separate test source file
    //    TestSmsProvider.java that implements NotificationChannelProvider
    //    with supportedChannel()=SMS, providerName()="test-sms"
    //    Compile it with JavaCompiler API and package with META-INF/services entry

    // 2. Write the jar to a temp directory
    Path tempDir = Files.createTempDirectory("plugins-test");
    // ... write jar bytes to tempDir/test-sms-provider.jar

    // 3. Create ProviderPluginLoader with mocked ProviderRepository
    ProviderRepository mockRepo = Mockito.mock(ProviderRepository.class);
    Mockito.when(mockRepo.findByProviderName("test-sms"))
           .thenReturn(Optional.empty());

    var loader = new ProviderPluginLoader(mockRepo, tempDir.toString());
    loader.loadPlugins();

    // 4. Assert registration
    var provider = loader.getProvider(Channel.SMS, "test-sms");
    assertThat(provider).isPresent();
    assertThat(provider.get().providerName()).isEqualTo("test-sms");
    assertThat(provider.get().supportedChannel()).isEqualTo(Channel.SMS);

    // 5. Assert DB upsert called
    Mockito.verify(mockRepo, Mockito.times(1)).save(Mockito.any());
}

@Test
void shouldWarnAndContinueWhenJarHasNoProviders() throws Exception {
    // Write an empty jar (no META-INF/services entry) to temp dir
    // Assert loadPlugins() completes without exception
    // Assert registry is empty for all channels
    // Assert WARN was logged (use LogCaptor or similar)
}

@Test
void shouldSkipMissingProviderNamesInGetProvidersOrdered() {
    // Manually register one provider in registry via reflection or
    // by calling loadJar with a real jar
    // Call getProvidersOrdered(SMS, List.of("registered", "missing"))
    // Assert result has 1 element (registered only)
}
```

### Verification

```bash
# 1. Unit tests pass
mvn test -pl notify-app -Dtest=PluginLoaderTest

# 2. Full build succeeds
mvn clean install -DskipTests
```

---

## Prompt 5 — App Module: Template Rendering & JSONPath Binding

### Goal

Implement `TemplateRenderer` — the component that takes a channel config
(body/subject/title as locale maps), a runtime payload, JSONPath bindings,
and produces a rendered `ChannelMessage` ready to hand to a provider.

### Architecture context

Read CLAUDE.md for full context. This component has no Spring Data or HTTP
dependencies — it is purely in-memory transformation. It must be thoroughly
unit-tested as rendering failures surface directly to callers as 422 errors.

### Dependencies to add to notify-app/pom.xml

```xml

    com.jayway.jsonpath
    json-path
    


    com.github.spullara.mustache.java
    compiler
    0.9.14

```

### Rendering pipeline

```
Input:
  channel             — which channel (SMS, EMAIL, etc.)
  templates           — Map<String, String> locale → Mustache template string
  payloadBindings     — Map<String, String> variable → JSONPath expression (nullable)
  payload             — Map<String, Object> the raw request payload
  locale              — requested locale string (nullable, defaults to "default")

Step 1 — Variable extraction:
  For each entry in payloadBindings:
    Evaluate the JSONPath expression against payload
    On PathNotFoundException → throw TemplateRenderException.badJsonPath(channel, expr, cause)
    Collect into Map<String, String> variables

Step 2 — Locale selection:
  Look up templates.get(locale)
  If absent → look up templates.get("default")
  If still absent → throw TemplateRenderException.missingDefaultLocale(channel)

Step 3 — Placeholder validation:
  Scan selected template string for all {{varName}} patterns
  For each varName not present in variables map →
    throw TemplateRenderException.missingVariable(channel, varName)

Step 4 — Mustache render:
  Compile template string with DefaultMustacheFactory
  Execute against variables map
  Return rendered string

Output:
  ChannelMessage(channel, renderedBody, renderedSubject, renderedTitle, emptyMap())
  For EMAIL: both body and subject templates must be rendered
  For PUSH:  both body and title templates must be rendered
  For SMS/WHATSAPP: only body
```

### Specification

#### Package

`org.digit.notify.app.template`

#### TemplateRenderException

```java
package org.digit.notify.app.template;

import org.digit.notify.spi.Channel;

public class TemplateRenderException extends RuntimeException {

    private final Channel channel;

    public TemplateRenderException(Channel channel, String message, Throwable cause) {
        super(message, cause);
        this.channel = channel;
    }

    public TemplateRenderException(Channel channel, String message) {
        super(message);
        this.channel = channel;
    }

    public Channel getChannel() { return channel; }

    public static TemplateRenderException missingVariable(
        Channel channel, String varName
    ) {
        return new TemplateRenderException(channel,
            "Template variable '" + varName + "' not resolved for channel " + channel);
    }

    public static TemplateRenderException badJsonPath(
        Channel channel, String expression, Throwable cause
    ) {
        return new TemplateRenderException(channel,
            "JSONPath expression '" + expression + "' failed for channel " + channel,
            cause);
    }

    public static TemplateRenderException missingDefaultLocale(Channel channel) {
        return new TemplateRenderException(channel,
            "No template found for 'default' locale for channel " + channel);
    }
}
```

#### TemplateRenderer

```java
@Component
public class TemplateRenderer {

    public ChannelMessage render(
        Channel channel,
        Map bodyTemplates,
        @Nullable Map subjectTemplates,
        @Nullable Map titleTemplates,
        @Nullable Map payloadBindings,
        Map payload,
        @Nullable String locale
    ) { ... }

    private Map extractVariables(
        Channel channel,
        Map bindings,
        Map payload
    ) { ... }

    private String selectTemplate(
        Channel channel,
        Map templates,
        String locale
    ) { ... }

    private String renderMustache(
        Channel channel,
        String template,
        Map variables
    ) { ... }
}
```

Implementation notes:

- `extractVariables`: use `JsonPath.parse(payload).read(expression, String.class)`.
  Wrap `PathNotFoundException` and `ClassCastException` in
  `TemplateRenderException.badJsonPath(...)`.
- `selectTemplate`: `locale` param defaults to `"default"` if null. Try
  `templates.get(locale)` first, then `templates.get("default")`. Throw
  `missingDefaultLocale` if both absent.
- `renderMustache`: before calling Mustache, extract all `{{varName}}` patterns
  from the template string using a simple regex `\{\{(\w+)\}\}` and verify each
  is present in the variables map. Throw `missingVariable` for first missing one.
  Then compile with `new DefaultMustacheFactory()` and execute.
- The `render()` method calls `extractVariables` once (shared across
  body/subject/title), then renders each non-null template map. Returns
  `ChannelMessage` with rendered body, subject (null for non-email), title
  (null for non-push).

#### Unit tests

Create `TemplateRendererTest` in `src/test/java/org/digit/notify/app/template/`:

```java
class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void happyPath_sms_rendersBodyWithExtractedVariables() {
        // payload: {"data": {"otp_code": "123456", "expiry": {"minutes": 10}}}
        // bindings: {"otp": "$.data.otp_code", "expiry": "$.data.expiry.minutes"}
        // template: "Your OTP is {{otp}}, expires in {{expiry}} minutes"
        // expected body: "Your OTP is 123456, expires in 10 minutes"
    }

    @Test
    void localeFallback_usesDefaultWhenRequestedLocaleAbsent() {
        // templates: {"default": "Your OTP is {{otp}}"}  (no "fr" key)
        // locale: "fr"
        // should succeed using "default" template
    }

    @Test
    void missingDefaultLocale_throwsTemplateRenderException() {
        // templates: {"en": "Your OTP is {{otp}}"}  (no "default" key)
        // locale: "fr"
        // expect TemplateRenderException with message containing "default"
    }

    @Test
    void missingVariable_throwsTemplateRenderExceptionWithVarName() {
        // template: "Hello {{name}}, your OTP is {{otp}}"
        // bindings: {"otp": "$.data.otp"} (no "name" binding)
        // expect TemplateRenderException with message containing "name"
    }

    @Test
    void badJsonPath_throwsTemplateRenderException() {
        // bindings: {"val": "$.nonexistent.deep.path"}
        // payload: {} (empty)
        // expect TemplateRenderException with message containing the expression
    }

    @Test
    void nullPayloadBindings_templateWithNoPlaceholders_rendersSuccessfully() {
        // bindings: null
        // template: "Hello from notify-service"  (no placeholders)
        // should succeed, rendered body equals the template
    }

    @Test
    void email_rendersBothBodyAndSubject() {
        // provide both bodyTemplates and subjectTemplates
        // assert returned ChannelMessage has non-null renderedSubject
    }

    @Test
    void sms_renderedSubjectIsNull() {
        // provide only bodyTemplates, subjectTemplates = null
        // assert returned ChannelMessage.renderedSubject() is null
    }
}
```

### Verification

```bash
# 1. All unit tests pass (no Spring context required)
mvn test -pl notify-app -Dtest=TemplateRendererTest

# 2. Full build succeeds
mvn clean install -DskipTests
```

---

## Prompt 6 — App Module: Provider Mapping Resolver & Dispatch Engine

### Goal

Implement `ProviderMappingResolver` and `DispatchEngine` — the core dispatch
logic that fans out to all channels concurrently, renders templates, resolves
providers with country-aware fallback, calls providers with ordered fallback,
and returns a full outcome including per-attempt audit records.

### Architecture context

Read CLAUDE.md for full context. Key design points:

- All four channels run concurrently using Java 25 virtual threads with
  `StructuredTaskScope`. Do NOT use a fixed thread pool.
- The engine does NOT persist anything — it returns a `DispatchOutcome` record
  containing both final results and attempt records. Persistence is handled by
  `NotificationService` in Prompt 7.
- Provider resolution is by name string, not by entity ID — the mapping stores
  names and the registry map is keyed by name.

### Specification

#### Package

`org.digit.notify.app.dispatch`

#### ProviderMappingResolver

```java
@Component
public class ProviderMappingResolver {

    private final ProviderMappingRepository mappingRepository;

    public ProviderMappingResolver(ProviderMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public List resolve(Channel channel, @Nullable String countryCode,
                                String tenantId) {
        // 1. If countryCode non-null: try country-specific mapping
        // 2. Fall back to global (country IS NULL) mapping
        // 3. If neither found: return empty list
        // Return the providers list from the found mapping
    }
}
```

#### DispatchOutcome and AttemptRecord

```java
package org.digit.notify.app.dispatch;

import org.digit.notify.spi.Channel;
import org.digit.notify.spi.DispatchResult;
import org.digit.notify.spi.DispatchStatus;
import org.jspecify.annotations.Nullable;
import java.time.Instant;
import java.util.List;

public record DispatchOutcome(
    List results,
    List attempts
) {}

public record AttemptRecord(
    Channel channel,
    String providerName,
    int attemptNo,
    DispatchStatus status,
    @Nullable String reason,
    Instant attemptedAt
) {}
```

#### NotifyRequest model

Create in `org.digit.notify.app.model` (used by engine, service, and controller):

```java
package org.digit.notify.app.model;

import org.digit.notify.spi.Recipient;
import org.jspecify.annotations.Nullable;
import java.util.Map;

public record NotifyRequest(
    String templateCode,
    Recipient recipient,
    Map payload,
    @Nullable String locale,
    Map metadata
) {}
```

#### DispatchEngine

```java
@Component
public class DispatchEngine {

    private final ProviderMappingResolver mappingResolver;
    private final ProviderPluginLoader pluginLoader;
    private final TemplateRenderer templateRenderer;

    // constructor injection

    public DispatchOutcome dispatch(
        NotifyRequest request,
        NotificationConfigEntity config,
        String tenantId
    ) {
        var results = new CopyOnWriteArrayList();
        var attempts = new CopyOnWriteArrayList();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (Channel channel : Channel.values()) {
                scope.fork(() -> {
                    var result = dispatchChannel(
                        channel, request, config, tenantId, attempts);
                    results.add(result);
                    return null;
                });
            }
            scope.join();
            // do NOT call throwIfFailed() — channel failures are handled
            // per-channel and captured as FAILED DispatchResults
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Dispatch interrupted", e);
        }

        return new DispatchOutcome(List.copyOf(results), List.copyOf(attempts));
    }

    private DispatchResult dispatchChannel(
        Channel channel,
        NotifyRequest request,
        NotificationConfigEntity config,
        String tenantId,
        List attempts
    ) {
        // 1. Get channel config — if null or !enabled → return skipped
        // 2. Render template via TemplateRenderer
        //    → on TemplateRenderException → record FAILED attempt, return failed
        // 3. Resolve provider names via ProviderMappingResolver
        //    → if empty → record FAILED attempt, return failed
        // 4. Get ordered providers from PluginLoader
        //    → if empty after filtering → record FAILED attempt, return failed
        // 5. Attempt loop with fallback:
        //    for each provider (attemptNo starting at 1):
        //      try provider.send(message, recipient, metadata)
        //      record AttemptRecord regardless of outcome
        //      if DISPATCHED → return dispatched result immediately
        //      if FAILED/exception → log warn, continue to next
        // 6. If all exhausted → return failed result
    }
}
```

#### Unit tests

Create `DispatchEngineTest` in `src/test/java/org/digit/notify/app/dispatch/`:

```java
@ExtendWith(MockitoExtension.class)
class DispatchEngineTest {

    @Mock ProviderMappingResolver mappingResolver;
    @Mock ProviderPluginLoader pluginLoader;
    @Mock TemplateRenderer templateRenderer;
    @Mock NotificationChannelProvider smsMockProvider;

    @InjectMocks DispatchEngine engine;

    @Test
    void allChannelsEnabled_allProvidersSucceed_returns4DispatchedResults() {
        // Setup: config with all 4 channels enabled
        // Mock: templateRenderer returns ChannelMessage for each channel
        // Mock: mappingResolver returns ["mock-provider"] for each channel
        // Mock: pluginLoader returns [smsMockProvider] for each channel
        // Mock: smsMockProvider.send() returns DispatchResult.dispatched(...)
        // Assert: outcome.results() has 4 entries all DISPATCHED
        // Assert: outcome.attempts() has 4 entries (one per channel)
    }

    @Test
    void channelDisabled_returnsSkippedForThatChannel() {
        // Config with SMS disabled, others enabled
        // Assert: SMS result is SKIPPED, no attempt record for SMS
        // Assert: other 3 channels attempted
    }

    @Test
    void primaryProviderFails_fallbackSucceeds_returnsDispatched() {
        // Setup: SMS channel only (others disabled)
        // Mock: mappingResolver returns ["primary", "fallback"]
        // Mock: pluginLoader returns [primaryMock, fallbackMock]
        // Mock: primaryMock.send() returns DispatchResult.failed(...)
        // Mock: fallbackMock.send() returns DispatchResult.dispatched(...)
        // Assert: final result is DISPATCHED
        // Assert: 2 attempt records for SMS (attemptNo 1=FAILED, 2=DISPATCHED)
    }

    @Test
    void allProvidersFailForChannel_returnsFailedResult() {
        // All providers return failed
        // Assert: result is FAILED
        // Assert: attempt records count equals number of providers tried
    }

    @Test
    void noProviderMapping_returnsFailedWithExplanation() {
        // mappingResolver returns empty list
        // Assert: result is FAILED, reason contains "no provider mapping"
    }

    @Test
    void templateRenderFails_returnsFailedWithRenderException() {
        // templateRenderer throws TemplateRenderException
        // Assert: result is FAILED, reason contains render exception message
    }
}
```

### Verification

```bash
# 1. All unit tests pass
mvn test -pl notify-app -Dtest=DispatchEngineTest

# 2. Full build succeeds
mvn clean install -DskipTests
```

---

## Prompt 7 — App Module: Notification Orchestration Service

### Goal

Implement `NotificationService` — the top-level `@Service` that orchestrates
the full send flow, provides CRUD for configs and mappings, manages provider
status, and handles all typed exceptions.

### Architecture context

Read CLAUDE.md for full context. This is the service layer that the REST
controllers in Prompt 8 will call directly. All business rules and validation
live here, not in controllers.

### Typed exceptions

Create all in package `org.digit.notify.app.exception`:

```java
public class EntityNotFoundException extends RuntimeException {
    private final String entityType;
    private final String id;

    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " not found with id: " + id);
        this.entityType = entityType;
        this.id = id;
    }
    // getters
}

public class DuplicateConfigException extends RuntimeException {
    public DuplicateConfigException(String tenantId, String templateCode) {
        super("NotificationConfig already exists for tenant '" + tenantId
            + "' and templateCode '" + templateCode + "'");
    }
}

public class DuplicateMappingException extends RuntimeException {
    public DuplicateMappingException(String tenantId, String channel,
                                     @Nullable String country) {
        super("ProviderMapping already exists for tenant '" + tenantId
            + "', channel '" + channel + "', country '" + country + "'");
    }
}

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

### Response model

Create in `org.digit.notify.app.model`:

```java
public record NotifyResponse(
    String notificationId,
    String templateCode,
    List channels
) {}

public record ChannelDispatchStatus(
    String channel,
    String status,
    @Nullable String provider,
    @Nullable String reason
) {}
```

### Specification

#### Dependencies to add to notify-app/pom.xml

```xml

    com.github.f4b6a3
    ulid-creator
    5.2.3

```

#### NotificationService

```java
@Service
public class NotificationService {

    private final NotificationConfigRepository configRepository;
    private final ProviderMappingRepository mappingRepository;
    private final ProviderRepository providerRepository;
    private final NotificationLogRepository logRepository;
    private final NotificationAttemptRepository attemptRepository;
    private final DispatchEngine dispatchEngine;

    // constructor injection of all above

    // ---- Send ----

    @Transactional
    public NotifyResponse sendNotification(NotifyRequest request, String tenantId) {
        // 1. Resolve config
        var config = configRepository
            .findByTenantIdAndTemplateCodeAndIsActiveTrue(tenantId, request.templateCode())
            .orElseThrow(() -> new EntityNotFoundException(
                "NotificationConfig", request.templateCode()));

        // 2. Dispatch
        var outcome = dispatchEngine.dispatch(request, config, tenantId);

        // 3. Generate notificationId
        String notificationId = "ntf_" + UlidCreator.getUlid().toString();

        // 4. Persist log
        var log = new NotificationLogEntity();
        log.setNotificationId(notificationId);
        log.setTenantId(tenantId);
        log.setTemplateCode(request.templateCode());
        log.setRecipientRef(resolveRecipientRef(request.recipient()));
        log.setCreatedAt(Instant.now());
        logRepository.save(log);

        // 5. Persist attempts
        var attemptEntities = outcome.attempts().stream().map(a -> {
            var entity = new NotificationAttemptEntity();
            entity.setNotificationId(notificationId);
            entity.setChannel(a.channel().name());
            entity.setProviderName(a.providerName());
            entity.setAttemptNo(a.attemptNo());
            entity.setStatus(a.status().name());
            entity.setReason(a.reason());
            entity.setAttemptedAt(a.attemptedAt());
            return entity;
        }).toList();
        attemptRepository.saveAll(attemptEntities);

        // 6. Build response
        var channelStatuses = outcome.results().stream().map(r ->
            new ChannelDispatchStatus(
                r.channel().name(),
                r.status().name(),
                r.providerName(),
                r.reason()
            )
        ).toList();

        return new NotifyResponse(notificationId, request.templateCode(), channelStatuses);
    }

    private String resolveRecipientRef(Recipient recipient) {
        if (recipient.email() != null) return recipient.email();
        if (recipient.phone() != null) return recipient.phone();
        return "unknown";
    }

    // ---- NotificationConfig CRUD ----

    @Transactional
    public NotificationConfigEntity createConfig(
        NotificationConfigEntity entity, String tenantId
    ) {
        configRepository.findByTenantIdAndTemplateCode(tenantId, entity.getTemplateCode())
            .stream().findAny().ifPresent(existing -> {
                throw new DuplicateConfigException(tenantId, entity.getTemplateCode());
            });
        entity.setTenantId(tenantId);
        entity.setActive(true);
        entity.getAuditDetail().setCreatedTime(Instant.now());
        return configRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public NotificationConfigEntity getConfig(UUID id, String tenantId) {
        return configRepository.findById(id)
            .filter(c -> c.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("NotificationConfig", id.toString()));
    }

    @Transactional(readOnly = true)
    public List listConfigs(
        String tenantId,
        @Nullable String templateCode,
        @Nullable Boolean isActive
    ) {
        if (templateCode != null) {
            return configRepository.findByTenantIdAndTemplateCode(tenantId, templateCode);
        }
        if (isActive != null) {
            return configRepository.findByTenantIdAndIsActive(tenantId, isActive);
        }
        return configRepository.findByTenantId(tenantId);
    }

    @Transactional
    public NotificationConfigEntity updateConfig(
        UUID id, NotificationConfigEntity updated, String tenantId
    ) {
        var existing = getConfig(id, tenantId);
        existing.setTemplateCode(updated.getTemplateCode());
        existing.setChannels(updated.getChannels());
        existing.setActive(updated.isActive());
        existing.getAuditDetail().setLastModifiedTime(Instant.now());
        return configRepository.save(existing);
    }

    @Transactional
    public void deleteConfig(UUID id, String tenantId) {
        var existing = getConfig(id, tenantId);
        configRepository.delete(existing);
    }

    // ---- ProviderMapping CRUD ----

    @Transactional
    public ProviderMappingEntity createMapping(
        ProviderMappingEntity entity, String tenantId
    ) {
        // Check uniqueness
        if (entity.getCountry() != null) {
            mappingRepository.findByTenantIdAndChannelAndCountry(
                tenantId, entity.getChannel(), entity.getCountry()
            ).ifPresent(e -> {
                throw new DuplicateMappingException(
                    tenantId, entity.getChannel(), entity.getCountry());
            });
        } else {
            mappingRepository.findByTenantIdAndChannelAndCountryIsNull(
                tenantId, entity.getChannel()
            ).ifPresent(e -> {
                throw new DuplicateMappingException(tenantId, entity.getChannel(), null);
            });
        }

        // Validate all provider names exist in registry
        entity.getProviders().forEach(name ->
            providerRepository.findByProviderName(name).orElseThrow(() ->
                new ValidationException("Provider '" + name + "' not found in registry"))
        );

        entity.setTenantId(tenantId);
        entity.getAuditDetail().setCreatedTime(Instant.now());
        return mappingRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List listMappings(
        String tenantId, @Nullable String channel
    ) {
        if (channel != null) {
            return mappingRepository.findByTenantIdAndChannel(tenantId, channel);
        }
        return mappingRepository.findAll().stream()
            .filter(m -> m.getTenantId().equals(tenantId))
            .toList();
    }

    @Transactional
    public ProviderMappingEntity updateMapping(
        UUID id, ProviderMappingEntity updated, String tenantId
    ) {
        var existing = mappingRepository.findById(id)
            .filter(m -> m.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("ProviderMapping", id.toString()));
        existing.setProviders(updated.getProviders());
        existing.getAuditDetail().setLastModifiedTime(Instant.now());
        return mappingRepository.save(existing);
    }

    @Transactional
    public void deleteMapping(UUID id, String tenantId) {
        var existing = mappingRepository.findById(id)
            .filter(m -> m.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("ProviderMapping", id.toString()));
        mappingRepository.delete(existing);
    }

    // ---- Provider operations ----

    @Transactional(readOnly = true)
    public List listProviders(
        @Nullable String channel, @Nullable Boolean isActive
    ) {
        if (isActive != null) {
            return providerRepository.findByIsActive(isActive);
        }
        return providerRepository.findAll();
    }

    @Transactional
    public ProviderEntity updateProviderStatus(UUID id, boolean isActive) {
        var entity = providerRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Provider", id.toString()));
        entity.setActive(isActive);
        entity.getAuditDetail().setLastModifiedTime(Instant.now());
        return providerRepository.save(entity);
    }
}
```

#### Unit tests

Create `NotificationServiceTest`:

```java
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationConfigRepository configRepository;
    @Mock ProviderMappingRepository mappingRepository;
    @Mock ProviderRepository providerRepository;
    @Mock NotificationLogRepository logRepository;
    @Mock NotificationAttemptRepository attemptRepository;
    @Mock DispatchEngine dispatchEngine;

    @InjectMocks NotificationService service;

    @Test
    void sendNotification_happyPath_returnsResponseWithNtfPrefix() {
        // Mock config found
        // Mock dispatchEngine returns outcome with 3 DISPATCHED + 1 SKIPPED
        // Assert response.notificationId() starts with "ntf_"
        // Assert response.channels() has 4 entries
        // Verify logRepository.save() called once
        // Verify attemptRepository.saveAll() called with correct size
    }

    @Test
    void sendNotification_configNotFound_throwsEntityNotFoundException() {
        // configRepository returns empty
        // Assert EntityNotFoundException thrown
        // Verify dispatchEngine never called
    }

    @Test
    void createMapping_duplicateCountrySpecific_throwsDuplicateMappingException() {
        // mappingRepository.findByTenantIdAndChannelAndCountry returns present
        // Assert DuplicateMappingException thrown
    }

    @Test
    void createMapping_unknownProviderName_throwsValidationException() {
        // mappingRepository returns empty (no duplicate)
        // providerRepository.findByProviderName returns empty
        // Assert ValidationException thrown with provider name in message
    }
}
```

### Verification

```bash
# 1. Unit tests pass
mvn test -pl notify-app -Dtest=NotificationServiceTest

# 2. Full build succeeds
mvn clean install -DskipTests
```

---

## Prompt 8 — App Module: REST Controllers & Exception Handling

### Goal

Implement all REST endpoints from the OpenAPI spec, the global exception handler,
the response header filter, and MapStruct mappers.

### Architecture context

Read CLAUDE.md for full context. The API spec to implement exactly is at:
https://raw.githubusercontent.com/digitnxt/digit-specs/refs/heads/notify/v3.0.0/notify.yaml

All endpoint paths, field names, status codes, and headers must match that spec.
Do not invent anything not in the spec.

### Dependencies to add to notify-app/pom.xml

```xml

    org.mapstruct
    mapstruct
    1.6.3


    org.mapstruct
    mapstruct-processor
    1.6.3
    provided

```

Add `mapstruct-processor` to the `maven-compiler-plugin` annotation processor
paths in `notify-app/pom.xml`.

### Specification

#### Package structure

```
org.digit.notify.app.controller
org.digit.notify.app.controller.dto
org.digit.notify.app.controller.mapper
```

#### Request/Response DTOs

Create records in `org.digit.notify.app.controller.dto` matching the OpenAPI
schema field names exactly. Key ones:

```java
// POST /notify request body
public record NotifyRequestDto(
    @NotBlank String templateCode,
    @NotNull RecipientDto recipient,
    @NotNull Map payload,
    String locale,
    Map metadata
) {}

public record RecipientDto(
    String phone,
    String email,
    List deviceTokens,
    String countryCode,
    Map metadata
) {}

// POST /notify response
public record NotifyResponseDto(
    String notificationId,
    String templateCode,
    List channels
) {}

public record ChannelDispatchStatusDto(
    String channel,
    String status,
    String provider,
    String reason
) {}

// Error response (used by GlobalExceptionHandler)
public record ErrorDto(
    String code,
    String message,
    String timestamp,
    String traceId
) {}
```

Create DTOs for `NotificationConfig`, `ProviderMapping`, `Provider` request and
response bodies matching the OpenAPI spec schemas.

#### NotifyController

```java
@RestController
@RequestMapping("/notify")
public class NotifyController {

    private final NotificationService notificationService;
    private final NotifyMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NotifyResponseDto notify(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestBody @Valid NotifyRequestDto requestDto
    ) {
        var request = mapper.toDomain(requestDto);
        var response = notificationService.sendNotification(request, tenantId);
        return mapper.toDto(response);
    }
}
```

#### NotificationConfigController

```java
@RestController
@RequestMapping("/notification-configs")
public class NotificationConfigController {

    @PostMapping         // 201 Created
    @GetMapping          // 200 OK, query params: templateCode, isActive
    @GetMapping("/{id}") // 200 OK
    @PutMapping("/{id}") // 200 OK
    @DeleteMapping("/{id}") // 204 No Content
}
```

#### ProviderMappingController

```java
@RestController
@RequestMapping("/provider-mappings")
public class ProviderMappingController {

    @PostMapping         // 201 Created
    @GetMapping          // 200 OK, query param: channel
    @PutMapping("/{id}") // 200 OK
    @DeleteMapping("/{id}") // 204 No Content
}
```

#### ProviderController

```java
@RestController
@RequestMapping("/providers")
public class ProviderController {

    @GetMapping                       // 200 OK, query params: channel, isActive
    @PatchMapping("/{id}/status")     // 200 OK, body: { "isActive": true }
}
```

#### GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return error("NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler({DuplicateConfigException.class, DuplicateMappingException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDto handleConflict(RuntimeException ex, HttpServletRequest req) {
        return error("CONFLICT", ex.getMessage(), req);
    }

    @ExceptionHandler({ValidationException.class,
                       ConstraintViolationException.class,
                       MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto handleBadRequest(Exception ex, HttpServletRequest req) {
        return error("BAD_REQUEST", ex.getMessage(), req);
    }

    @ExceptionHandler(TemplateRenderException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorDto handleRenderError(TemplateRenderException ex, HttpServletRequest req) {
        return error("RENDER_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleGeneric(Exception ex, HttpServletRequest req) {
        return error("INTERNAL_ERROR", "An unexpected error occurred", req);
    }

    private ErrorDto error(String code, String message, HttpServletRequest req) {
        return new ErrorDto(
            code,
            message,
            Instant.now().toString(),
            (String) req.getAttribute("X-Request-ID")
        );
    }
}
```

#### ResponseHeaderFilter

```java
@Component
public class ResponseHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String requestId = Optional.ofNullable(request.getHeader("X-Request-ID"))
            .orElse(UUID.randomUUID().toString());
        String correlationId = request.getHeader("X-Correlation-ID");
        String tenantId = request.getHeader("X-Tenant-ID");

        MDC.put("requestId", requestId);
        MDC.put("tenantId", tenantId != null ? tenantId : "");

        try {
            chain.doFilter(request, response);
        } finally {
            response.setHeader("X-Request-ID", requestId);
            response.setHeader("X-Response-Time",
                String.valueOf(System.currentTimeMillis() - startTime) + "ms");
            response.setHeader("X-Response-Timestamp", Instant.now().toString());
            if (correlationId != null) {
                response.setHeader("X-Correlation-ID", correlationId);
            }
            if (tenantId != null) {
                response.setHeader("X-Tenant-ID", tenantId);
            }
            MDC.clear();
        }
    }
}
```

#### MapStruct mappers

```java
@Mapper(componentModel = "spring")
public interface NotifyMapper {
    NotifyRequest toDomain(NotifyRequestDto dto);
    Recipient toDomain(RecipientDto dto);
    NotifyResponseDto toDto(NotifyResponse response);
}

@Mapper(componentModel = "spring")
public interface NotificationConfigMapper {
    NotificationConfigEntity toEntity(NotificationConfigRequestDto dto);
    NotificationConfigResponseDto toDto(NotificationConfigEntity entity);
}

@Mapper(componentModel = "spring")
public interface ProviderMappingMapper {
    ProviderMappingEntity toEntity(ProviderMappingRequestDto dto);
    ProviderMappingResponseDto toDto(ProviderMappingEntity entity);
}
```

#### Update application.yml

Add springdoc config:

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /docs
```

#### Integration tests

Create `NotifyControllerIT`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class NotifyControllerIT {

    @Autowired MockMvc mockMvc;
    @MockBean NotificationService notificationService;

    @Test
    void postNotify_validRequest_returns202WithNotificationId() throws Exception {
        // Mock service returns NotifyResponse with ntf_ prefixed id
        // POST /notify with valid body and X-Tenant-ID header
        // Assert status 202
        // Assert response body contains notificationId starting with "ntf_"
        // Assert response has X-Response-Time header
    }

    @Test
    void postNotify_missingTenantIdHeader_returns400() throws Exception {
        // POST /notify without X-Tenant-ID header
        // Assert status 400
    }

    @Test
    void postNotify_serviceThrowsEntityNotFoundException_returns404() throws Exception {
        // Mock service throws EntityNotFoundException
        // Assert status 404
        // Assert response body has code = "NOT_FOUND"
    }
}
```

### Verification

```bash
# 1. Integration tests pass
mvn test -pl notify-app -Dtest=NotifyControllerIT

# 2. Full build succeeds
mvn clean install -DskipTests
```

---

## Prompt 9 — Assembly, Configuration & Docker

### Goal

Wire up the complete runnable application: full `application.yml`, local dev
profile, multi-stage Dockerfile, Docker Compose, and README.

### Architecture context

Read CLAUDE.md for full context. At this point all application code is complete.
This prompt makes it deployable. The `/providers` mount point in the container
is where operator drops provider jars. The app has no provider code of its own.

### Specification

#### notify-app/src/main/resources/application.yml (replace existing)

```yaml
server:
  port: 8080
  shutdown: graceful

spring:
  application:
    name: notify-service
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/notify}
    username: ${SPRING_DATASOURCE_USERNAME:notify}
    password: ${SPRING_DATASOURCE_PASSWORD:notify}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      pool-name: notify-hikari
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
  lifecycle:
    timeout-per-shutdown-phase: 30s

notify:
  plugins:
    directory: ${NOTIFY_PLUGINS_DIR:/providers}

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /docs

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

#### notify-app/src/main/resources/application-local.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notify
    username: notify
    password: notify
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  flyway:
    baseline-on-migrate: true

notify:
  plugins:
    directory: ./providers

logging:
  level:
    org.digit.notify: DEBUG
```

#### notify-app/Dockerfile

```dockerfile
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build
COPY pom.xml .
COPY notify-spi/pom.xml notify-spi/
COPY notify-app/pom.xml notify-app/
RUN mvn dependency:go-offline -B -q
COPY notify-spi/src notify-spi/src
COPY notify-app/src notify-app/src
RUN mvn package -pl notify-app -am -DskipTests -B -q

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S notify && adduser -S notify -G notify
WORKDIR /app
COPY --from=builder /build/notify-app/target/notify-app-*.jar app.jar
RUN mkdir /providers && chown notify:notify /providers
USER notify
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

#### docker-compose.yml (at repo root)

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: notify
      POSTGRES_USER: notify
      POSTGRES_PASSWORD: notify
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U notify"]
      interval: 5s
      timeout: 5s
      retries: 5

  notify-app:
    build:
      context: .
      dockerfile: notify-app/Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/notify
      SPRING_DATASOURCE_USERNAME: notify
      SPRING_DATASOURCE_PASSWORD: notify
    volumes:
      - ./providers:/providers
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  postgres_data:
```

Also create an empty `providers/` directory at repo root with a `.gitkeep` inside
it. This is the local development provider drop directory.

#### README.md (at repo root)

Write a README covering:

1. Architecture overview — two modules, external provider jars, plugin loading
2. Prerequisites — Java 25, Maven 3.9+, Docker 24+
3. Local development — `docker compose up postgres -d` then
   `mvn spring-boot:run -pl notify-app -Dspring-boot.run.profiles=local`
4. Full stack with Docker — `docker compose up --build`
5. How to implement a provider (step by step):
   - Create a new Maven project
   - Depend on `notify-spi:1.0.0-SNAPSHOT` with `provided` scope
   - Implement `NotificationChannelProvider`
   - Add `META-INF/services/org.digit.notify.spi.NotificationChannelProvider`
   - Configure `maven-shade-plugin` to produce a fat jar excluding `notify-spi`
   - Build with `mvn package`
   - Copy jar to `./providers/` and restart the service
   - Call `GET /providers` to confirm registration
6. Environment variables reference table
7. API docs URL — `http://localhost:8080/docs`

### Verification

```bash
# 1. Docker build succeeds
docker compose build

# 2. Full stack starts
docker compose up -d

# 3. Health check passes
curl -f http://localhost:8080/actuator/health

# 4. Swagger UI accessible
curl -f http://localhost:8080/docs

# 5. Full Maven build still passes
mvn clean install -DskipTests
```

---

## Prompt 10 — Reference Provider Template (separate repo)

### Goal

Create `notify-provider-template` — a standalone Maven project in a separate
directory (NOT inside notify-service). This is the template repo that any
provider author forks to build a new provider.

### Architecture context

Read CLAUDE.md for full context. Key constraints:

- This project has NO parent POM relationship with notify-service
- It depends ONLY on notify-spi as `provided` scope
- It produces a shaded fat jar that excludes notify-spi
- It uses NO Spring — providers run outside the Spring application context,
  loaded by URLClassLoader at runtime
- Configuration must come from environment variables (no @Value, no @ConfigurationProperties)
- The `ServiceLoader` mechanism requires a public no-arg constructor

### Specification

#### Directory

Create at `../notify-provider-template/` (sibling of notify-service, NOT inside it)

#### pom.xml

```xml
org.digit.notify
notify-provider-template
1.0.0-SNAPSHOT
jar
Template for implementing a notify-service channel provider


  25
  25
  25
  1.0.0-SNAPSHOT



  
    org.digit.notify
    notify-spi
    ${notify.spi.version}
    provided
  
  
    org.slf4j
    slf4j-simple
    2.0.16
  
  
    org.junit.jupiter
    junit-jupiter
    5.11.0
    test
  
  
    org.assertj
    assertj-core
    3.26.3
    test
  



  
    
      org.apache.maven.plugins
      maven-shade-plugin
      3.6.0
      
        
          package
          shade
          
            false
            false
            
              
            
            
              
                org.digit.notify:notify-spi
              
            
          
        
      
    
  

```

#### Source files

`src/main/java/org/digit/notify/provider/template/TemplateProviderConfig.java`:

```java
package org.digit.notify.provider.template;

public class TemplateProviderConfig {

    private final String apiKey;
    private final String fromAddress;

    public TemplateProviderConfig() {
        this.apiKey = System.getenv("NOTIFY_PROVIDER_TEMPLATE_API_KEY");
        this.fromAddress = System.getenv("NOTIFY_PROVIDER_TEMPLATE_FROM_ADDRESS");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "NOTIFY_PROVIDER_TEMPLATE_API_KEY environment variable is required");
        }
    }

    public String getApiKey() { return apiKey; }
    public String getFromAddress() { return fromAddress; }
}
```

`src/main/java/org/digit/notify/provider/template/TemplateProvider.java`:

```java
package org.digit.notify.provider.template;

import org.digit.notify.spi.Channel;
import org.digit.notify.spi.ChannelMessage;
import org.digit.notify.spi.DispatchResult;
import org.digit.notify.spi.NotificationChannelProvider;
import org.digit.notify.spi.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class TemplateProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(TemplateProvider.class);

    // ServiceLoader requires a public no-arg constructor
    public TemplateProvider() {
        log.info("TemplateProvider initialised");
        // TODO: initialise TemplateProviderConfig here once you have set env vars
        // new TemplateProviderConfig();
    }

    @Override
    public Channel supportedChannel() {
        return Channel.SMS; // TODO: change to your target channel
    }

    @Override
    public String providerName() {
        return "template"; // TODO: change to your provider name e.g. "twilio"
    }

    @Override
    public DispatchResult send(
        ChannelMessage message,
        Recipient recipient,
        Map metadata
    ) {
        // TODO: implement your provider API call here
        //
        // For SMS/WhatsApp use:  message.renderedBody(), recipient.phone()
        // For Email use:         message.renderedSubject(), message.renderedBody(),
        //                        recipient.email()
        // For Push use:          message.renderedTitle(), message.renderedBody(),
        //                        recipient.deviceTokens()
        //
        // On success: return DispatchResult.dispatched(supportedChannel(), providerName())
        // On failure: return DispatchResult.failed(supportedChannel(), providerName(), reason)
        //
        // Do NOT throw exceptions — always return a DispatchResult.
        // The dispatch engine handles fallback based on DispatchStatus.

        throw new UnsupportedOperationException(
            "TemplateProvider.send() not implemented — this is a template");
    }
}
```

`src/main/resources/META-INF/services/org.digit.notify.spi.NotificationChannelProvider`:

```
org.digit.notify.provider.template.TemplateProvider
```

#### Test

`src/test/java/org/digit/notify/provider/template/TemplateProviderTest.java`:

```java
class TemplateProviderTest {

    @Test
    void providerName_returnsTemplate() {
        var provider = new TemplateProvider();
        assertThat(provider.providerName()).isEqualTo("template");
    }

    @Test
    void supportedChannel_returnsSms() {
        var provider = new TemplateProvider();
        assertThat(provider.supportedChannel()).isEqualTo(Channel.SMS);
    }

    @Test
    void send_throwsUnsupportedOperationException_documentingTodoForImplementors() {
        var provider = new TemplateProvider();
        var message = new ChannelMessage(
            Channel.SMS, "Test body", null, null, Map.of());
        var recipient = new Recipient(
            "+1234567890", null, List.of(), "IN", Map.of());
        assertThatThrownBy(() -> provider.send(message, recipient, Map.of()))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
```

#### README.md

Write a README covering:

1. What this is — a template to fork for new notify-service providers
2. Prerequisites — Java 25, Maven 3.9, a running notify-service instance
3. Step-by-step implementation guide:
   - Fork/copy this repo, rename artifact ID
   - Change `supportedChannel()` return value
   - Change `providerName()` return value (must be unique across all providers)
   - Implement `send()` using your vendor SDK
   - Add vendor SDK dependency to pom.xml (it will be shaded into the fat jar)
   - Set required environment variables
4. Why `notify-spi` is `provided` scope — classloader isolation explanation
   in plain language
5. Why dependencies must be shaded — fat jar requirement explanation
6. How to test locally:
   - `mvn package`
   - Copy `target/notify-provider-template-*.jar` to `./providers/` in notify-service
   - Restart notify-service (or `docker compose restart notify-app`)
   - `GET /providers` — confirm your provider appears
   - `POST /provider-mappings` — wire your provider to a channel
   - `POST /notify` — fire a test notification
7. Environment variables table

### Verification

```bash
# 1. Build produces fat jar
mvn package

# 2. notify-spi NOT bundled in the fat jar (must return empty)
jar tf target/notify-provider-template-*.jar | grep "digit/notify/spi"

# 3. META-INF/services entry IS present
jar tf target/notify-provider-template-*.jar | grep "META-INF/services"

# 4. Tests pass
mvn test
```