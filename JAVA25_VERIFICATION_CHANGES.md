# Java 25 Verification — Change Log

Branch under test: `ChakshuGautam:feat/spring-boot-3.5-virtual-threads` (checked out locally as `vt-java25`).
Goal: build each service on JDK 25, run it, smoke-test APIs, produce a pass/fail table.

This file logs **every change** made to the working tree or environment during verification.

---

## Environment changes
- **Installed Amazon Corretto JDK 25** (`25.0.3`) via `brew install --cask corretto@25` (user ran the sudo step).
  Path: `/Library/Java/JavaVirtualMachines/amazon-corretto-25.jdk/Contents/Home`. Previously only JDK 17 + 8 were present.

## Git / working tree
- Checked out fork branch `feat/spring-boot-3.5-virtual-threads` (commit `063f3198fb`) as local branch `vt-java25`.

## Build configuration
- **No source/pom edits persisted.** A temporary `<annotationProcessorPaths>` edit to
  `core-services/libraries/services-common/pom.xml` was used to diagnose the Lombok failure, then **reverted**.
- **Root cause found:** JDK 23+ disables annotation processing from the classpath by default, so Lombok
  (`@Slf4j`, `@Data`, getters) silently stops generating code → mass `cannot find symbol` errors.
  The branch sets `<java.version>25</java.version>` but never re-enabled annotation processing.
- **Fix applied at build time (no code change):** all builds run with `-Dmaven.compiler.proc=full`,
  which re-enables classpath annotation processing. Lombok is already a dependency on every module, so no
  pom edits are required. Confirmed working on `services-common` and `tracer`.
- **Second build issue:** several services pin an old `<lombok.version>1.18.36</lombok.version>` in their
  poms, which crashes under JDK 25 with `ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`.
  Affected (so far): `egov-idgen`, `egov-enc-service`, `egov-filestore`, `egov-indexer`, `egov-localization`.
  **Fix (no code change):** build with `-Dlombok.version=1.18.44`, which overrides the pom property.
- **Combined build flags used for all modules:** `-Dmaven.compiler.proc=full -Dlombok.version=1.18.44`.

---

## Database setup (for the run phase)
- Created local Postgres databases referenced by service configs:
  `auditdb, chat, dataupload, devdb, devserverdb, egov, egovdb, enc_service, mseva, newdb, nssdb, pgr, rainmaker_new, testdb`
  (plus the default `postgres`). No schemas/migrations loaded yet — tables are created by separate DIGIT
  db-init steps in real deployments, so DB-backed API calls may fail until migrations are run.

## Per-service changes (pom edits — these CANNOT be fixed by CLI flags)
1. **`core-services/gateway/pom.xml`** — Lombok dependency version hardcoded `1.18.36` → **`1.18.44`**
   (hardcoded on the dependency, so `-Dlombok.version` override didn't apply). Fixes `TypeTag :: UNKNOWN`.
2. **`core-services/mdms-v2/pom.xml`** — Lombok dependency version `1.18.36` → **`1.18.44`** (same reason).
3. **`core-services/egov-notification-sms/pom.xml`** — `aspectjweaver` `1.8.10` → **`1.9.24`**.
   The old 1.8.10 jar has a malformed zip64 structure that JDK 25 refuses to open
   (`Invalid CEN header (invalid zip64 extra data field size)`).

### Build outcome: 39 / 39 modules BUILD SUCCESS on JDK 25
(4 libraries + 35 services). Required: global flags `-Dmaven.compiler.proc=full -Dlombok.version=1.18.44`
plus the 3 pom edits above.

---

## Runtime phase

### Infra started
- Postgres 16 (reinstalled to fix icu4c linkage), role `postgres/postgres` created, per-service DBs created.
- Kafka + Zookeeper already running. Redis + Elasticsearch NOT installed (services needing them are noted).

### Systemic runtime config bug found (branch issue, NOT a Java 25 issue)
- Every DB-backed service sets `spring.datasource.driver-class-name=io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver`
  but leaves `spring.datasource.url=jdbc:postgresql://...`. The OTel driver only accepts `jdbc:otel:postgresql://...`,
  so startup fails: *"Driver OpenTelemetryDriver claims to not accept jdbcUrl"*. 0 services use the correct `jdbc:otel:` URL.
- **Test-time override (not persisted to code):** services are launched with
  `--spring.datasource.driver-class-name=org.postgresql.Driver` so they can boot and serve DB APIs.
  (Real fix for the branch: either prefix URLs with `jdbc:otel:` or drop the OTel driver-class-name override.)
- Other launch overrides (test-time only): `--server.port=18080`,
  `--management.endpoints.web.exposure.include=*`, `--spring.datasource.username/password=postgres`.

### Smoke-test definition
- **RUNNING** = Spring Boot logs "Started …Application" and the port binds.
- **HEALTH** = actuator health endpoint returns HTTP 200 `{"status":"UP"}`.

### Runtime fix APPLIED (committed) — OpenTelemetry JDBC URL
The OTel-driver/plain-URL mismatch is now fixed **in the source** (no launch override needed):
- For the 15 DB services that use `driver-class-name=...OpenTelemetryDriver` **and** have a
  `spring.datasource.url`, the URL was changed `jdbc:postgresql://...` → **`jdbc:otel:postgresql://...`**
  so the OTel driver accepts it. OTel DB tracing is preserved.
  (audit-service, boundary-service, egov-accesscontrol, egov-enc-service, egov-filestore, egov-idgen,
  egov-indexer, egov-localization, egov-persister, egov-pg-service, egov-url-shortening, egov-user-event,
  egov-workflow-v2, mdms-v2, service-request)
- `spring.flyway.url` left as `jdbc:postgresql://...` on purpose — Flyway auto-detects the plain driver from
  that prefix; `jdbc:otel:` is not a prefix Flyway recognizes.
- **egov-location**: removed a duplicate/conflicting `driver-class-name` line (it had both OTel and plain;
  plain won by last-wins). Now cleanly uses `org.postgresql.Driver` + `jdbc:postgresql://` like household.
- The 4 OTel-driver services with **no** `datasource.url` (egov-mdms-service, egov-notification-mail,
  egov-notification-sms, gateway) were left untouched — no URL to fix; they don't create a datasource here.
- **Verified** (launched with no driver override): `egov-idgen` (shared datasource → Flyway) and
  `egov-pg-service` (separate `flyway.url`) both boot, Flyway migrates, no "driver claims to not accept" error.
