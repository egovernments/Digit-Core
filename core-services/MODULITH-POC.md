# DIGIT Modulith PoC — Declarative Monolith Bundling for Core Services

**Branch:** `modulith` &nbsp;|&nbsp; **Scope:** `egov-idgen` + `egov-mdms-service` &nbsp;|&nbsp; **Status:** working end-to-end, three deployment shapes verified

---

## 1. TL;DR

Two DIGIT services (`egov-idgen`, `egov-mdms-service`) that today run as independent Spring Boot processes can now — from the **same source code** — also boot as a **single bundled Spring Boot process** where the idgen→MDMS call is an in-process function call instead of an HTTP hop.

The composition is **declarative**: a `package.yaml` at the repo root names which services to bundle, per-service `service.yaml` files declare capabilities/dependencies, and a custom Maven plugin (`digit-bundler-maven-plugin`) generates the runnable bundle on demand.

**No change** to how standalone deployments work: `cd egov-idgen && mvn spring-boot:run` and `cd egov-mdms-service && mvn spring-boot:run` behave exactly as before.

---

## 2. Why (Motivation)

- Each DIGIT service today runs in its own JVM, deployed via k8s. That's the right shape for large production installs but overkill for smaller deployments and dev environments.
- Every inter-service call crosses HTTP (serialize → network → deserialize), even when both services are on the same node.
- DIGIT would like a **modulith** deployment option: bundle a chosen set of services into one process to reduce operational footprint (single container, single DB pool, single JVM to observe) while keeping the ability to peel them back apart for scale.
- Goal of this PoC: prove that a **single manifest + a build-time bundler** is enough machinery to achieve this, without forking codebases or maintaining separate "monolith" branches.

---

## 3. High-level Design

Three cooperating pieces:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    core-services/  (maven aggregator)               │
│                                                                     │
│   package.yaml                              ┌─────────────────┐     │
│   ─────────────                             │ tools/bundler/  │     │
│   bundles:                          reads   │  (maven plugin) │     │
│     idgen-monolith: ───────────────────────▶│                 │     │
│       include: [egov-idgen, egov-mdms-svc]  │  digit-bundler: │     │
│       properties: {...}                     │    generate     │     │
│                                             └────────┬────────┘     │
│   egov-idgen/service.yaml                            │              │
│   egov-mdms-service/service.yaml   also read ────────┤              │
│                                                      │              │
│                                                      ▼              │
│                                          ┌───────────────────────┐  │
│                                          │  bundles/             │  │
│                                          │  idgen-monolith/      │  │
│                                          │  (generated, gitignore│  │
│                                          │   pom + main + props) │  │
│                                          └───────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

- **Service-level refactors** make each service *composable*.
- **Manifests** (`service.yaml` per service, `package.yaml` at the root) declare who talks to whom and how they should be composed.
- **Bundler** reads the manifests and emits a maven module ready to run as a monolith.

---

## 4. The Four Refactor Pillars (per service)

Each service was made composable through four small changes. Neither service's business logic changed.

### 4.1 Move HTTP paths from `server.servlet.context-path` into controllers

**Before**: `server.servlet.context-path=/egov-idgen` in `application.properties`. Only one context path per servlet context — fine for standalone, breaks in a monolith where two controllers must share one context.

**After**: controllers own their path via `@RequestMapping`.

```java
// IdGenerationController.java
@RestController
@RequestMapping(path = "/egov-idgen/id/")
public class IdGenerationController { ... }

// MDMSController.java
@RestController
@RequestMapping(value = "/egov-mdms-service/v1")
public class MDMSController { ... }
```

Now both can share one servlet context, each answering only its own prefix.

### 4.2 Introduce an `MdmsClient` interface, swap implementations by property

The single inter-service touchpoint is idgen→MDMS. The old code injected `MdmsClientService` (a `RestTemplate`-based HTTP client) directly.

New shape:

```
libraries/mdms-client/src/main/java/org/egov/mdms/service/
├── MdmsClient.java              interface (new)
└── HttpMdmsClient.java          renamed from MdmsClientService
                                 @ConditionalOnProperty("egov.mdms.mode=http",
                                                        matchIfMissing=true)

egov-mdms-service/.../infra/mdms/service/
└── LocalMdmsClient.java         @ConditionalOnProperty("egov.mdms.mode=local")
                                 calls MDMSService.searchMaster in-process
```

idgen's `MdmsService` now autowires the *interface*:

```java
@Autowired
MdmsClient mdmsClient;   // Spring picks the impl based on the property
```

Bean-selection matrix:

| Boot form | `egov.mdms.mode` | Active `MdmsClient` |
|---|---|---|
| idgen standalone | `http` (default) | `HttpMdmsClient` (network call to remote MDMS) |
| Monolith | `local` (set by bundler) | `LocalMdmsClient` (direct method call to `MDMSService`) |

The one line that replaces the HTTP call, in `LocalMdmsClient.getMaster(MdmsCriteriaReq)`:

```java
Map<String, Map<String, JSONArray>> result = mdmsService.searchMaster(mdmsCriteriaReq);
```

### 4.3 Split property files: `<service>-defaults.properties` + thin `application.properties`

Two files per service, playing distinct roles:

| File | Purpose | Shipped in published jar? |
|---|---|---|
| `<service>-defaults.properties` | Real config: business rules, DB tables, feature flags. Same content whether standalone or bundled. | Yes |
| `application.properties` | Standalone bootstrap glue: `spring.config.import`, `server.port`, default `egov.mdms.mode=http`. | **Excluded via `maven-jar-plugin`** (would otherwise conflict with the bundle's own `application.properties`) |

Result: the same jar serves both modes without conflict.

### 4.4 Defensive isolation at the `LocalMdmsClient` boundary

**Concern raised during review**: HTTP serialization was accidentally providing deep-copy isolation between caller and callee. In-process, MDMS returns objects that alias `MDMSApplicationRunnerImpl`'s static `TenantMap` cache — a caller's mutation on the response would corrupt master data process-wide.

**Where the fix lives**: `LocalMdmsClient.getMaster()` — the "pretend to be HTTP" class. `MDMSService` stays pure; only in-process callers pay the isolation cost. HTTP callers already got isolation for free via the wire.

**How it's done**: Jackson round-trip.

```java
private MdmsResponse isolate(MdmsResponse response) {
    return objectMapper.readValue(
        objectMapper.writeValueAsBytes(response),
        MdmsResponse.class);
}
```

Serialize the response to bytes, deserialize back into a fresh object graph. Byte-identical semantics to what HTTP was doing on the wire.

#### Why Jackson round-trip, not a cheap structural copy?

A hand-rolled per-level copy (walk the outer `Map`, inner `Map`, and each `JSONArray` with `addAll`) is faster but only isolates the outer three levels. The innermost elements — the `LinkedHashMap` records inside each `JSONArray` — remain aliased to the cache. A caller doing `((Map)response.getMdmsRes().get("mod").get("master").get(0)).put("k", "v")` would still corrupt master data.

To *fully* deep-copy without Jackson, you'd need a recursive walker over arbitrary JSON — ~15 lines, correct today but silently breaks isolation if `MdmsResponse` ever gains a new nested type.

Cost comparison (order-of-magnitude for a typical MDMS response):

| Approach | Per-call cost | Coverage | Failure mode |
|---|---|---|---|
| Levels 1-3 structural copy (`addAll`) | ~10-50 µs | Partial — leaves per-record mutation aliased | Silent cache corruption if callers touch inner Maps |
| Recursive deep walker | ~50-200 µs | Full, but hand-written | Silently misses new schema types |
| **Jackson round-trip (chosen)** | ~200-800 µs | Full, matches HTTP by construction | Schema-driven, robust to `MdmsResponse` changes |
| Baseline HTTP hop (what we're replacing) | ~5-50 ms | Full | — |

Jackson is ~10× slower than the cheap partial copy but ~50× faster than the HTTP hop it replaces. MDMS's own lookup work (JsonPath filtering, JSON tree walking) is already in the millisecond range, so the isolation cost is within the noise of what MDMS is doing anyway.

**Why we picked it despite the cost**:
1. **Match HTTP semantics.** Under HTTP the response was serialized through Jackson on the wire — any lesser isolation is a behavior change from standalone mode.
2. **Robust to schema evolution.** A future field or nested type gets deep-copied for free — no walker code to update.
3. **One line vs. thirty.** For a PoC boundary that fires once per MDMS call at sub-ms cost, marginal perf isn't worth the maintenance surface.

The tradeoff flips at high throughput (thousands of MDMS calls/s) or very large responses — at that point, the recursive walker becomes the middle-ground answer.

---

## 5. The Manifests

### 5.1 Per-service `service.yaml` — capabilities & consumers

Each service ships a single `service.yaml` next to its `pom.xml`. It's the service's machine-readable "contract" — what it offers, what it needs, and the metadata the bundler needs to fold it into a composed module.

**Concrete files in this PoC:**

```yaml
# egov-idgen/service.yaml
schema-version: 1
name: egov-idgen
scan-base-packages:
  - org.egov
  - org.egov.id
context-path: /egov-idgen
main-class-hint: org.egov.PtIdGenerationApplication
defaults-file: idgen-defaults.properties
provides: []
consumes:
  - capability: mdms
    switch-property: egov.mdms.mode
```

```yaml
# egov-mdms-service/service.yaml
schema-version: 1
name: egov-mdms-service
scan-base-packages:
  - org.egov
  - org.egov.infra.mdms
context-path: /egov-mdms-service
main-class-hint: org.egov.MDMSApplication
defaults-file: mdms-defaults.properties
provides:
  - capability: mdms
    local-value: local
    http-value: http
consumes: []
```

#### 5.1.1 Field reference

| Field | Type | Required | What the bundler does with it |
|---|---|---|---|
| `schema-version` | integer | yes | Version stamp for the manifest format; reserved for future migrations. |
| `name` | string | yes | Must equal the service's directory name under `core-services/`. Used to look up `<name>/service.yaml` when resolving entries in `package.yaml`'s `include:` list, and to attribute conflict warnings. |
| `scan-base-packages` | list of strings | yes | Union'ed across all included services to form `@SpringBootApplication(scanBasePackages = {...})` on the generated main class. Must cover every package containing `@RestController`, `@Service`, `@Component`, `@Configuration` that must be picked up in the bundle. |
| `context-path` | string | optional | Documentation only in the current implementation — controllers own their prefix via `@RequestMapping` after the refactor. Kept in the manifest so future tooling can detect prefix collisions between two services in the same bundle. |
| `main-class-hint` | string | optional | Documentation only today. The bundler generates a fresh main class per bundle (e.g. `IdgenMonolithApplication`). Reserved for a future "reuse existing main class" bundling mode. |
| `defaults-file` | string | yes | Filename inside `src/main/resources/` that holds this service's real configuration (excluding the standalone-only `application.properties`). The bundler wires it into the generated bundle's `spring.config.import=` chain and reads it to detect cross-service property conflicts. |
| `provides` | list of `{capability, local-value, http-value}` | yes (may be empty) | Pass 1 of `computeSwitchFlags`: builds a map `capability → local-value` from every provider in the bundle. If empty, this service offers no in-process capability to peers. `local-value` and `http-value` are the property values that consumers' `@ConditionalOnProperty` will match against (typically `local` / `http`). |
| `consumes` | list of `{capability, switch-property}` | yes (may be empty) | Pass 2 of `computeSwitchFlags`: for every consumed capability whose provider is in the same bundle, the bundler emits `<switch-property>=<provider's local-value>` into the generated `application.properties`. If the provider is *not* in the bundle, no line is emitted and the consumer's `@ConditionalOnProperty(..., matchIfMissing=true)` default kicks in (HTTP mode). |

#### 5.1.2 What each field actually drives, at a glance

| Editing this... | ...causes... |
|---|---|
| `scan-base-packages` | Different set of Spring beans is scanned when the bundle boots. Shrinking may drop your controllers; widening may pull in unwanted `@Component`s from transitive deps. |
| `defaults-file` | Bundle's `spring.config.import` chain changes on next `generate`. The file on disk must exist at `src/main/resources/<value>`. |
| `provides` | Toggles whether peers in the same bundle detect you as a local implementation. Removing an entry forces consumers back to HTTP even when co-bundled. |
| `consumes` | Tells the bundler which property to flip in the generated `application.properties` when a provider is present. `switch-property` and its accepted values must match the `@ConditionalOnProperty` annotations in the actual client code. |
| `context-path`, `main-class-hint` | Nothing today. Reserved metadata for future features. |

#### 5.1.3 Implicit alignment with code

`service.yaml` is a *declaration*; it's meaningful only if the code agrees.

- The `switch-property` name and the `local-value` / `http-value` strings must match the `@ConditionalOnProperty(name=..., havingValue=...)` annotations on the client implementations.
- The `defaults-file` must actually exist at `src/main/resources/<value>` and must be paired with a `maven-jar-plugin` exclusion of `application.properties` (so the standalone bootstrap file doesn't leak into a bundle's classpath and shadow the bundle's own `application.properties`).
- The service's `pom.xml` `groupId`/`artifactId`/`version` — read directly by the bundler — must be current, since the generated bundle pom pins these versions in its `<dependency>` blocks.

#### 5.1.4 Minimum viable `service.yaml` for a new service

If a future service (say `egov-user`) wants to be bundleable as a provider:

```yaml
schema-version: 1
name: egov-user
scan-base-packages: [org.egov, org.egov.user]
defaults-file: user-defaults.properties
provides:
  - capability: user
    local-value: local
    http-value: http
consumes: []
```

Plus, in code: a `UserClient` interface, a `HttpUserClient` (in a shared library) and a `LocalUserClient` (in the service module) with matching `@ConditionalOnProperty(name = "egov.user.mode", ...)` — mirroring exactly the mdms pattern.

### 5.2 Bundle manifest `package.yaml` — composition decision

```yaml
# core-services/package.yaml
schema-version: 1

bundles:
  idgen-monolith:
    type: monolith
    include: [egov-idgen, egov-mdms-service]
    spring-profiles: [monolith]
    server:
      port: 8080
    properties:
      spring.datasource.url: jdbc:postgresql://localhost:5432/rainmaker_new
      spring.datasource.username: postgres
      spring.datasource.password: postgres
      egov.mdms.conf.path: file:///Users/aniket/Documents/egov-mdms-data/data/pb
      masters.config.url: file:///Users/aniket/Documents/egov-mdms-data/master-config.json
```

Adding a third service to the monolith = editing `include: [...]` and running the bundler again.

---

## 6. The Bundler (`digit-bundler-maven-plugin`)

Located at `tools/bundler/`. ~300 LOC Java, one mojo (`generate`), one runtime dependency (SnakeYAML).

### 6.1 What `mvn digit-bundler:generate -Dbundle=idgen-monolith` does

1. **Read `package.yaml`** → find the target bundle.
2. **For each service in `include:`** → load its `service.yaml` and maven coordinates from its `pom.xml`.
3. **Union all `scan-base-packages`** → single set for the generated `@SpringBootApplication`.
4. **Compute switch flags**: for each consumer's declared capability, if a provider in the same bundle offers it, emit the consumer's `switch-property` set to the provider's `local-value`.

    ```
    egov-idgen consumes "mdms" (switch: egov.mdms.mode)
    egov-mdms-service provides "mdms" (local-value: "local")
    → emit: egov.mdms.mode=local
    ```

5. **Detect property conflicts**: load each service's defaults file; for keys defined by 2+ services with different values, emit a `[WARNING]` naming the conflicting key, each service's value, and which one will win (last in `include:` order). Silences automatically for keys the bundle explicitly overrides.
6. **Emit files** into `bundles/<bundle-name>/`:

    ```
    bundles/idgen-monolith/
    ├── pom.xml                                    # spring-boot-parent 3.4.5 + service deps
    ├── src/main/java/org/egov/bundle/
    │   └── IdgenMonolithApplication.java          # @SpringBootApplication(scanBasePackages={...})
    └── src/main/resources/
        ├── application.properties                 # profiles, port, spring.config.import, switch flags
        └── application-monolith.properties        # env-specific overrides (loaded last → always win)
    ```

7. **Idempotently register** the generated module in the root reactor `pom.xml`.

### 6.2 Property precedence — why we split into two generated files

Spring Boot loads config data in this order (low → high precedence):

1. The bundle's own `application.properties` (importing document)
2. Each `spring.config.import` target (defaults files) — later imports override earlier
3. `application-<profile>.properties` (highest)
4. Command-line args / env vars

Bundle-level overrides from `package.yaml`'s `properties:` block go into `application-monolith.properties`, so they always beat both defaults files. The bundler surfaces silent conflicts at generation time via the property-conflict warning.

---

## 7. Runtime — What Happens on One Request

**Standalone idgen** → HTTP hop:

```
curl POST http://localhost:8088/egov-idgen/id/_generate
  → IdGenerationController → IdGenerationService → MdmsService
  → mdmsClient.getMaster()                        // HttpMdmsClient wins @ConditionalOnProperty
  → RestTemplate.postForObject("http://…:8094/…")  // HTTP: serialize, network, deserialize
```

**Monolith** → in-process:

```
curl POST http://localhost:8080/egov-idgen/id/_generate
  → IdGenerationController → IdGenerationService → MdmsService
  → mdmsClient.getMaster()                        // LocalMdmsClient wins @ConditionalOnProperty
  → mdmsService.searchMaster(request)             // direct method call, same JVM, same thread
```

Same call site, same interface method. Only the implementation swaps. Zero code change in the flow between the two forms.

---

## 8. Running & Testing — Step-by-Step

### 8.1 Prerequisites

| What | Version tested | Notes |
|---|---|---|
| Java | 17 | Both services use `spring-boot-starter-parent:3.4.5` |
| Maven | 3.9+ | Repo uses `<packaging>pom</packaging>` reactor |
| PostgreSQL | 14.x | `rainmaker_new` DB, user/pass `postgres/postgres` on `localhost:5432` |
| Local MDMS data | any recent clone | Cloned at `~/Documents/egov-mdms-data/` (adjust paths in `package.yaml` if elsewhere) |

Verify:

```bash
java -version                       # 17.x
mvn -version                        # 3.9+
psql -U postgres -h localhost -c 'SELECT 1'
psql -U postgres -h localhost -lqt | cut -d\| -f1 | grep -w rainmaker_new  # exists
ls ~/Documents/egov-mdms-data/master-config.json   # exists
```

### 8.2 First-time build (install all artifacts)

Order matters — the library goes first, then the two services (both depend on it), then the bundler plugin (needed to generate the bundle).

```bash
cd ~/Documents/Digit-Core/core-services

# 1. shared library (contains MdmsClient interface + HttpMdmsClient)
cd libraries/mdms-client && mvn -q clean install -DskipTests && cd -

# 2. mdms-service (contains LocalMdmsClient)
cd egov-mdms-service && mvn -q clean install -DskipTests && cd -

# 3. idgen
cd egov-idgen && mvn -q clean install -DskipTests && cd -

# 4. bundler plugin
cd tools/bundler && mvn -q clean install -DskipTests && cd -
```

Confirm all four are in the local repo:

```bash
ls ~/.m2/repository/org/egov/mdms-client/2.9.1-SNAPSHOT/*.jar
ls ~/.m2/repository/org/egov/mdms/egov-mdms-service-test/2.9.1-SNAPSHOT/*.jar
ls ~/.m2/repository/org/egov/egov-idgen/2.9.3-SNAPSHOT/*.jar
ls ~/.m2/repository/org/egov/tools/digit-bundler-maven-plugin/1.0.0-SNAPSHOT/*.jar
```

### 8.3 Mode A — Standalone MDMS (`:8094`)

**Start**:

```bash
cd egov-mdms-service
mvn spring-boot:run -Dspring-boot.run.arguments="\
--egov.mdms.conf.path=file:///Users/aniket/Documents/egov-mdms-data/data/pb \
--masters.config.url=file:///Users/aniket/Documents/egov-mdms-data/master-config.json \
--otel.traces.exporter=none \
--otel.instrumentation.kafka.enabled=false"
```

**Test — MDMS search**:

```bash
curl -X POST 'http://localhost:8094/egov-mdms-service/v1/_search' \
  -H 'Content-Type: application/json' \
  -d '{
    "RequestInfo": {},
    "MdmsCriteria": {
      "tenantId": "pb",
      "moduleDetails": [
        {"moduleName":"common-masters","masterDetails":[{"name":"IdFormat"}]}
      ]
    }
  }'
```

**Expected**: `HTTP 200`, JSON body `{"ResponseInfo":null,"MdmsRes":{...}}` (may be `{}` if the local data folder has no `IdFormat` file for `pb`).

Startup log should contain:

```
HttpMdmsClient active (egov.mdms.mode=http). Target: http://localhost:8080/egov-mdms-service/v1/_search
Started MDMSApplication in ~7 seconds
Tomcat started on port 8094 (http) with context path '/'
```

**Stop**: `Ctrl-C`, or from another shell:

```bash
lsof -iTCP:8094 -sTCP:LISTEN -t | xargs -r kill
```

### 8.4 Mode B — Standalone idgen (`:8088`) with mdms on `:8094`

**Start** (assumes MDMS from 8.3 is still running):

```bash
cd egov-idgen
mvn spring-boot:run -Dspring-boot.run.arguments="\
--mdms.service.host=http://localhost:8094/ \
--otel.traces.exporter=none \
--otel.instrumentation.kafka.enabled=false"
```

**Test — generate an ID with inline format**:

```bash
curl -X POST 'http://localhost:8088/egov-idgen/id/_generate' \
  -H 'Content-Type: application/json' \
  -d '{
    "RequestInfo": {"apiId":"test","ver":"1.0","ts":0,"action":"POST"},
    "idRequests": [
      {"idName":"pt.ack.number","tenantId":"pb","format":"PT-[cy:yyyy-MM-dd]-[SEQ_EG_PT_ACK]"}
    ]
  }'
```

**Expected**: `HTTP 200`, body `{"idResponses":[{"id":"PT-YYYY-MM-DD-000NNN"}]}` — sequence increments each call.

**Test — multiple IDs in one call**:

```bash
curl -X POST 'http://localhost:8088/egov-idgen/id/_generate' \
  -H 'Content-Type: application/json' \
  -d '{
    "RequestInfo": {"apiId":"test","ver":"1.0","ts":0,"action":"POST"},
    "idRequests": [
      {"idName":"pt.ack.number","tenantId":"pb","format":"PT-[cy:yyyy-MM-dd]-[SEQ_EG_PT_ACK]"},
      {"idName":"tl.app.number","tenantId":"pb","format":"TL-[cy:yyyy]-[SEQ_TL_APP]"}
    ]
  }'
```

Startup log should contain:

```
HttpMdmsClient active (egov.mdms.mode=http). Target: http://localhost:8094/egov-mdms-service/v1/_search
Flyway ... Successfully validated 58 migrations
Started PtIdGenerationApplication in ~7 seconds
Tomcat started on port 8088 (http) with context path '/'
```

Note: `LocalMdmsClient` does NOT appear in idgen's log — the mdms-service jar is not on idgen's classpath in standalone mode.

**Stop**:

```bash
lsof -iTCP:8088 -sTCP:LISTEN -t | xargs -r kill
```

### 8.5 Mode C — Monolith (`:8080`, both services in one process)

**Step 1: generate the bundle** (from repo root):

```bash
cd ~/Documents/Digit-Core/core-services
mvn org.egov.tools:digit-bundler-maven-plugin:1.0.0-SNAPSHOT:generate -Dbundle=idgen-monolith
```

**Expected output** (bundler emits, `[WARNING]` on any cross-service property conflicts):

```
[WARNING] Property conflict: 'otel.instrumentation.http.server.ignore-urls'
    egov-idgen = /egov-idgen/health,/egov-idgen/promethus
    egov-mdms-service = /egov-mdms-service/health,/egov-mdms-service/promethus
    -> effective value: from egov-mdms-service (last in include order). Override in package.yaml `properties:` to silence.
[WARNING] Property conflict: 'otel.service.name'
    egov-idgen = egov-idgen
    egov-mdms-service = egov-mdms-service
    -> effective value: from egov-mdms-service (last in include order). Override in package.yaml `properties:` to silence.
[WARNING] 2 property conflict(s) detected across included services' defaults files.
[INFO] Bundle 'idgen-monolith' generated:
[INFO]   type          = monolith
[INFO]   services      = [egov-idgen, egov-mdms-service]
[INFO]   scan-packages = [org.egov, org.egov.id, org.egov.infra.mdms]
[INFO]   switch flags  = {egov.mdms.mode=local}
[INFO]   output        = /Users/aniket/Documents/Digit-Core/core-services/bundles/idgen-monolith
```

Inspect the generated files:

```bash
ls bundles/idgen-monolith/
cat bundles/idgen-monolith/src/main/resources/application.properties
cat bundles/idgen-monolith/src/main/resources/application-monolith.properties
```

**Step 2: run the bundle**:

```bash
cd bundles/idgen-monolith
mvn spring-boot:run
```

Startup log should contain (in order):

```
The following 1 profile is active: "monolith"
LocalMdmsClient active (egov.mdms.mode=local). In-process MDMS calls; no HTTP.
Flyway ... Successfully validated 58 migrations
Reading files from: file:///Users/aniket/Documents/egov-mdms-data/data/pb
Started IdgenMonolithApplication in ~7 seconds
Tomcat started on port 8080 (http) with context path '/'
```

**Test — idgen endpoint on the monolith**:

```bash
curl -X POST 'http://localhost:8080/egov-idgen/id/_generate' \
  -H 'Content-Type: application/json' \
  -d '{
    "RequestInfo": {"apiId":"test","ver":"1.0","ts":0,"action":"POST"},
    "idRequests": [
      {"idName":"pt.ack.number","tenantId":"pb","format":"PT-[cy:yyyy-MM-dd]-[SEQ_EG_PT_ACK]"}
    ]
  }'
```

**Expected**: `HTTP 200`, body `{"idResponses":[{"id":"PT-YYYY-MM-DD-000NNN"}]}`. Under the hood: `IdGenerationService` → idgen's `MdmsService` → `mdmsClient.getMaster()` (interface) → `LocalMdmsClient.getMaster()` → `MDMSService.searchMaster()` — all in-process.

**Test — mdms endpoint on the same port**:

```bash
curl -X POST 'http://localhost:8080/egov-mdms-service/v1/_search' \
  -H 'Content-Type: application/json' \
  -d '{
    "RequestInfo": {},
    "MdmsCriteria": {
      "tenantId": "pb",
      "moduleDetails": [
        {"moduleName":"common-masters","masterDetails":[{"name":"IdFormat"}]}
      ]
    }
  }'
```

**Expected**: `HTTP 200`. Both endpoints served by the same Tomcat, both hitting the same `MDMSService` bean, no HTTP hop between them.

**Prove no HTTP hop is happening** (port 8094 must be free):

```bash
lsof -iTCP:8094 -sTCP:LISTEN   # should print nothing
```

The idgen call still succeeds — because MDMS is invoked in-process, not over the network.

**Stop**:

```bash
lsof -iTCP:8080 -sTCP:LISTEN -t | xargs -r kill
```

### 8.6 Regenerating the bundle (idempotent)

Whenever `package.yaml`, any `service.yaml`, or any `<service>-defaults.properties` changes, regenerate:

```bash
cd ~/Documents/Digit-Core/core-services

# nuke the generated dir + de-register from root pom (bundler re-adds it)
rm -rf bundles/idgen-monolith
sed -i.bak 's|<module>bundles/idgen-monolith</module>||g' pom.xml && rm pom.xml.bak

# regenerate
mvn org.egov.tools:digit-bundler-maven-plugin:1.0.0-SNAPSHOT:generate -Dbundle=idgen-monolith
```

Or shorter — the bundler is idempotent; you can just regenerate on top:

```bash
mvn org.egov.tools:digit-bundler-maven-plugin:1.0.0-SNAPSHOT:generate -Dbundle=idgen-monolith
```

### 8.7 Cleanup — kill everything

```bash
for port in 8080 8088 8094; do
  lsof -iTCP:$port -sTCP:LISTEN -t 2>/dev/null | xargs -r kill
done
```

### 8.8 Format placeholder reference (for `_generate` payloads)

- `[cy:yyyy-MM-dd]` — current date, any Java date pattern (`cy:yyyy`, `cy:MMM`, etc.)
- `[fy:yyyy-yy]` — financial year (Apr–Mar)
- `[SEQ_<name>]` — Postgres sequence; auto-created if `autocreate.new.seq=true` (currently `false`, so the sequence must exist)
- `[city]` / `[TENANT_ID]` — tenant/city code
- `[d{n}]` — random digits

---

## 9. Verification (End-to-End)

All three deployment shapes tested from a clean rebuild.

| Mode | Command | Endpoint | Result |
|---|---|---|---|
| **mdms standalone** | `cd egov-mdms-service && mvn spring-boot:run` | `POST http://localhost:8094/egov-mdms-service/v1/_search` | HTTP 200 |
| **idgen standalone** (mdms up on :8094) | `cd egov-idgen && mvn spring-boot:run` | `POST http://localhost:8088/egov-idgen/id/_generate` | HTTP 200 → `PT-2026-07-27-000008`. Log: `HttpMdmsClient active. Target: http://localhost:8094/…` |
| **Monolith** | `mvn digit-bundler:generate -Dbundle=idgen-monolith` &nbsp;→&nbsp; `mvn -pl bundles/idgen-monolith spring-boot:run` | `POST http://localhost:8080/egov-idgen/id/_generate` | HTTP 200 → `PT-2026-07-27-000009`. Log: `LocalMdmsClient active. In-process MDMS calls; no HTTP`. Port 8094 not listening (proof no HTTP hop possible). |

Bundler property-conflict detection also verified — two legitimate clashes surfaced automatically on generation:

```
[WARNING] Property conflict: 'otel.instrumentation.http.server.ignore-urls'
    egov-idgen = /egov-idgen/health,/egov-idgen/promethus
    egov-mdms-service = /egov-mdms-service/health,/egov-mdms-service/promethus
    -> effective value: from egov-mdms-service (last in include order).

[WARNING] Property conflict: 'otel.service.name'
    egov-idgen = egov-idgen
    egov-mdms-service = egov-mdms-service
    -> effective value: from egov-mdms-service (last in include order).
```

Both are OTEL metadata leaks (not correctness bugs) — easy fix in `package.yaml`.

---

## 10. File-by-file Change Summary

| Path | Change | Rationale |
|---|---|---|
| `libraries/mdms-client/src/main/java/org/egov/mdms/service/MdmsClient.java` | **NEW** interface | Common contract for both HTTP and local impls |
| `libraries/mdms-client/.../HttpMdmsClient.java` | Renamed from `MdmsClientService`; added `@ConditionalOnProperty` | HTTP impl, default in standalone |
| `egov-mdms-service/.../LocalMdmsClient.java` | **NEW** — `implements MdmsClient` | In-process impl for monolith |
| `egov-mdms-service/.../MDMSService.java:165` | Added defensive shallow copy | Prevent cache aliasing when serialization no longer isolates callers |
| `egov-mdms-service/pom.xml` | Bump Spring Boot 3.2.2 → 3.4.5; add `classifier=exec`; exclude `application.properties` from jar | Unified SB version; plain jar for bundle deps; no property conflict |
| `egov-idgen/pom.xml` | Add `flyway-database-postgresql`; add `classifier=exec`; exclude `application.properties` | Managed Flyway 10.x needs postgres extension; same jar contract |
| `egov-mdms-service/service.yaml` | **NEW** manifest | Declares `mdms` capability |
| `egov-idgen/service.yaml` | **NEW** manifest | Declares `mdms` dependency + switch property |
| `<each>-defaults.properties` | **NEW** — split from `application.properties` | Reusable in both modes without conflict |
| Both `application.properties` | Now thin: 3 lines (import, port, mode) | Standalone bootstrap glue only |
| Both `Controller.java` | `@RequestMapping` carries the service prefix | Path ownership moved off servlet context |
| `package.yaml` | **NEW** bundle manifest | Composition decision |
| `tools/bundler/pom.xml`, `.../GenerateMojo.java` | **NEW** maven plugin | Reads manifests, emits bundle module |
| `.gitignore` | Add `bundles/` | Generated content, not committed |
| Root `pom.xml` | Register `tools/bundler` module | Plugin builds with the rest of the reactor |

---

## 11. Known Gaps / Not in Scope

- **Kafka / OTEL** wiring in monolith mode. OTEL config surfaces two legitimate conflicts today (service name, ignore-urls); resolving them in `package.yaml` is a one-minute follow-up.
- **Fat-jar packaging** for shipping the monolith as a container. `spring-boot-maven-plugin` default works but has not been size-tuned.
- **Auto-discovery** of new services from filesystem — bundler currently requires each service to have a hand-written `service.yaml`.
- **Third-service inclusion**. Pattern is proven with two services; extending to (say) `egov-user`, `egov-workflow-v2` requires: bump Spring Boot to 3.4.5, add controller `@RequestMapping`, add `service.yaml`, and split property files — same recipe.
- **Same-JVM property collisions.** Truly shared infra keys (e.g., `otel.service.name`, `server.port`, `spring.datasource.url`) can have only one value per JVM in a monolith. The bundler flags conflicts; humans must decide the value.
- **Runtime bean-collision safety**. Both services register into the same Spring context under `org.egov` scan root. No collisions in the current pair, but a future addition could clash on bean names — worth a plan-B `scanBasePackages` isolation strategy if it happens.

---

## 12. What This PoC Proves

1. Two services can share a JVM without source-forking, driven by a **single manifest** and a **standard `@ConditionalOnProperty` swap**.
2. Standalone and monolith deployments coexist off the same codebase — no separate branches, no shadow builds.
3. Composition can be **dynamic**: `mvn digit-bundler:generate` produces the bundle on demand from `package.yaml`, so new bundles are declarative edits, not new modules.
4. Real risks that HTTP hid (mutation aliasing, silent property clashes) can be surfaced and mitigated cheaply — defensive copy on the server side, generate-time conflict warnings in the bundler.

The design is ready to scale to more services with the same recipe.
