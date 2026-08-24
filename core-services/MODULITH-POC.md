# DIGIT Modulith PoC — Declarative Monolith Bundling for Core Services

**Branch:** `modulith` &nbsp;|&nbsp; **Scope:** `egov-idgen` + `egov-mdms-service` &nbsp;|&nbsp; **Status:** working end-to-end, three deployment shapes verified

---

## 1. TL;DR

Two DIGIT services (`egov-idgen`, `egov-mdms-service`) that today run as independent Spring Boot processes can now — from the **same source code** — also boot as a **single bundled Spring Boot process**. Inter-service calls still happen over HTTP, but in monolith mode the URL is a loopback back into the same JVM's Tomcat.

The composition is **declarative**: a `package.yaml` at the repo root names which services to bundle, per-service `service.yaml` files declare metadata the bundler needs, and a custom Maven plugin (`digit-bundler-maven-plugin`) generates the runnable bundle on demand.

**No change** to how standalone deployments work: `cd egov-idgen && mvn spring-boot:run` and `cd egov-mdms-service && mvn spring-boot:run` behave exactly as before.

**No change** to how services talk to each other: idgen calls MDMS with the same `MdmsClientService` (HTTP `RestTemplate`) in both modes. The only difference is what `mdms.service.host` points at.

---

## 2. Why (Motivation)

- Each DIGIT service today runs in its own JVM, deployed via k8s. That's the right shape for large production installs but overkill for smaller deployments and dev environments.
- DIGIT would like a **modulith** deployment option: bundle a chosen set of services into one process to reduce operational footprint (single container, single DB pool, single JVM to observe) while keeping the ability to peel them back apart for scale.
- Goal of this PoC: prove that a **single manifest + a build-time bundler** is enough machinery to achieve this, without forking codebases or maintaining separate "monolith" branches — and without needing any Java-level abstractions like conditional beans or in-process function-call fast paths.

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

- **Service-level refactors** make each service *composable* — moving URL prefixes into controllers and splitting property files.
- **Manifests** (`service.yaml` per service, `package.yaml` at the root) declare scan-packages and which services to bundle together.
- **Bundler** reads the manifests and emits a maven module ready to run as a monolith.

---

## 4. The Two Refactor Pillars (per service)

Each service was made composable through two small changes. Neither service's business logic changed, and the inter-service call code (`MdmsClientService`) is exactly the same as the original — no interface, no conditional beans, no in-process fast path.

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

Inter-service calls stay HTTP even in monolith mode — idgen's `MdmsClientService` posts to whatever `mdms.service.host` resolves to. In monolith the bundle sets it to `http://localhost:8080/`, so the call loops back into the same JVM's Tomcat. Same wire semantics, no need for a Java-level swap.

### 4.2 Split property files: `<service>-defaults.properties` + thin `application.properties`

Two files per service, playing distinct roles:

| File | Purpose | Shipped in published jar? |
|---|---|---|
| `<service>-defaults.properties` | Real config: business rules, DB tables, feature flags. Same content whether standalone or bundled. | Yes |
| `application.properties` | Standalone bootstrap glue: `spring.config.import`, `server.port`. | **Excluded via `maven-jar-plugin`** (would otherwise conflict with the bundle's own `application.properties`) |

Result: the same jar serves both modes without conflict. In the monolith, the generated `application.properties` at the bundle level takes over, and each service's `<service>-defaults.properties` still ships (as a library resource) and gets pulled in via `spring.config.import`.

---

## 5. The Manifests

### 5.1 Per-service `service.yaml` — bundler metadata

Each service ships a `service.yaml` next to its `pom.xml` carrying exactly two fields — everything the bundler actually reads to fold the service into a composed module.

**Concrete files in this PoC:**

```yaml
# egov-idgen/service.yaml
scan-base-packages:
  - org.egov
  - org.egov.id
defaults-file: idgen-defaults.properties
```

```yaml
# egov-mdms-service/service.yaml
scan-base-packages:
  - org.egov
  - org.egov.infra.mdms
defaults-file: mdms-defaults.properties
```

#### 5.1.1 Field reference

| Field | Type | What the bundler does with it |
|---|---|---|
| `scan-base-packages` | list of strings | Union'ed across all included services to form the bundle's `@ComponentScan(basePackages = {...})`. Must cover every package containing `@RestController`, `@Service`, `@Component`, `@Configuration` that must be picked up in the bundle. |
| `defaults-file` | string | Filename inside `src/main/resources/` that holds this service's real configuration (the file excluded from the standalone-only `application.properties`). The bundler wires it into the generated bundle's `spring.config.import=` chain and reads it to detect cross-service property conflicts. |

The service name is not carried in `service.yaml` — the `include:` entry in `package.yaml` (which must equal the directory name) is authoritative. The bundler injects it into the in-memory service spec at load time so downstream conflict-detection warnings can attribute rows by name.

#### 5.1.2 What each field actually drives, at a glance

| Editing this... | ...causes... |
|---|---|
| `scan-base-packages` | Different set of Spring beans is scanned when the bundle boots. Shrinking may drop your controllers; widening may pull in unwanted `@Component`s from transitive deps. |
| `defaults-file` | Bundle's `spring.config.import` chain changes on next `generate`. The file on disk must exist at `src/main/resources/<value>`. |

#### 5.1.3 Implicit alignment with code

`service.yaml` is a *declaration*; it's meaningful only if the code agrees.

- The `defaults-file` must actually exist at `src/main/resources/<value>` and must be paired with a `maven-jar-plugin` exclusion of `application.properties` (so the standalone bootstrap file doesn't leak into a bundle's classpath and shadow the bundle's own `application.properties`).
- The service's `pom.xml` `groupId`/`artifactId`/`version` — read directly by the bundler — must be current, since the generated bundle pom pins these versions in its `<dependency>` blocks.

#### 5.1.4 Minimum viable `service.yaml` for a new service

If a future service (say `egov-user`) wants to be bundleable:

```yaml
scan-base-packages: [org.egov, org.egov.user]
defaults-file: user-defaults.properties
```

That's it — two fields. If the new service needs to call other services, it does so with a plain HTTP client whose target URL is a bundle-level `properties:` entry in `package.yaml` (loopback URL in monolith, remote URL in standalone).

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
      egov.mdms.conf.path: /Users/aniket/Documents/egov-mdms-data/data/pb
      masters.config.url: file:///Users/aniket/Documents/egov-mdms-data/master-config.json
      # In-JVM HTTP loopback for idgen → MDMS
      mdms.service.host: http://localhost:8080/
```

Adding a third service to the monolith = editing `include: [...]` and running the bundler again. If the new service consumes another service via HTTP, add its client-URL property here too.

---

## 6. The Bundler (`digit-bundler-maven-plugin`)

Located at `tools/bundler/`. ~300 LOC Java, one mojo (`generate`), one runtime dependency (SnakeYAML).

### 6.1 What `mvn digit-bundler:generate -Dbundle=idgen-monolith` does

1. **Read `package.yaml`** → find the target bundle.
2. **For each service in `include:`** → load its `service.yaml` and maven coordinates from its `pom.xml`.
3. **Union all `scan-base-packages`** → single set for the generated main class's `@ComponentScan`.
4. **Detect property conflicts**: load each service's defaults file; for keys defined by 2+ services with different values, emit a `[WARNING]` naming the conflicting key, each service's value, and which one will win (last in `include:` order). Silences automatically for keys the bundle explicitly overrides.
5. **Emit files** into `bundles/<bundle-name>/`:

    ```
    bundles/idgen-monolith/
    ├── pom.xml                                    # spring-boot-parent 3.4.5 + service deps
    ├── src/main/java/org/egov/bundle/
    │   └── IdgenMonolithApplication.java          # main class with FQN-safe @ComponentScan
    └── src/main/resources/
        ├── application.properties                 # profiles, port, spring.config.import
        └── application-monolith.properties        # env-specific overrides from package.yaml
    ```

6. **Idempotently register** the generated module in the root reactor `pom.xml`.

### 6.2 Property precedence — why we split into two generated files

Spring Boot loads config data in this order (low → high precedence):

1. The bundle's own `application.properties` (importing document)
2. Each `spring.config.import` target (defaults files) — later imports override earlier
3. `application-<profile>.properties` (highest)
4. Command-line args / env vars

Bundle-level overrides from `package.yaml`'s `properties:` block go into `application-monolith.properties`, so they always beat both defaults files. The bundler surfaces silent conflicts at generation time via the property-conflict warning.

### 6.3 Bean-collision safety — FQN name generator

Two services sharing a scan root (as ours do — both live under `org.egov`) can, in principle, ship classes with identical simple names. Spring's default bean-name generator uses the *simple* class name (lowercased first letter), so two `NotificationService` classes in different packages would both resolve to bean name `notificationService` — a `BeanDefinitionOverrideException` at startup. To defend against this, the bundler emits a main class configured with `FullyQualifiedAnnotationBeanNameGenerator` and an exclude filter that removes any transitive `@SpringBootConfiguration` class from the scan.

Generated shape (`IdgenMonolithApplication.java`):

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = { "org.egov" },
    nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = SpringBootConfiguration.class
    )
)
public class IdgenMonolithApplication { ... }
```

Three deliberate choices, each fixing a specific failure mode we hit on the way:

**a. `FullyQualifiedAnnotationBeanNameGenerator` instead of the default.** Every component-scanned bean is registered under its full class FQN (e.g. `org.egov.mdms.service.MdmsClientService`) rather than the lower-camel simple name (`mdmsClientService`). Two services can each ship a class with the same simple name without collision — the packages disambiguate at Spring's bean name level, matching what Java's `import` statements already give at compile time. Type-based autowiring (`@Autowired MdmsClientService mdmsClientService`) is unaffected because the type match happens *before* the name lookup.

**b. `@SpringBootConfiguration` unrolled from `@SpringBootApplication` + explicit `@ComponentScan`.** `@SpringBootApplication` meta-annotates `@ComponentScan(...)` with default arguments (scan the class's own package). When you also put an explicit `@ComponentScan` on the same class, Spring processes both — running two scans, one under FQN naming (the explicit one), one under default naming (the meta one). That silently double-registers every bean. Splitting `@SpringBootApplication` into its three parts (`@SpringBootConfiguration`, `@EnableAutoConfiguration`, `@ComponentScan`) leaves exactly one scan directive on the class.

**c. `excludeFilters` for `@SpringBootConfiguration`.** Any `@SpringBootConfiguration`-annotated class on the classpath (each included service's main class plus any transitive library's — for us, `mdms-client.jar` ships an `org.egov.MdmsClientApplication`) carries its *own* meta-annotated `@ComponentScan` with default naming. If Spring picks these up as `@Configuration` beans, those secondary scans fire with default naming, re-registering every scanned class under both FQN and simple names. Filtering on `@SpringBootConfiguration` (which is meta-annotated by `@SpringBootApplication`) catches all of them generically.

**d. Overlap-free scan roots.** The bundler applies `minimalCovering()` on the union of `scan-base-packages` from all included services — if `org.egov` is present, `org.egov.id` and `org.egov.infra.mdms` are dropped. Overlapping scan roots can otherwise cause the same class to be visited multiple times by a single scan directive.

#### 6.3.1 What the FQN generator does NOT fix

Bean-name uniqueness is one specific correctness property. Other Spring context problems that FQN naming can't help with:

| Scenario | Fixed by FQN generator? | Actual fix |
|---|---|---|
| Two `@Service`/`@Component` classes with same simple name, different packages | Yes | — |
| Autowire-by-type in each service's own code | Works unchanged | — |
| Cross-service explicit reference by FQN type | Works | — |
| `@Qualifier("simpleName")` on the colliding class | Breaks — string no longer resolves | Either drop the qualifier (let type-based match), rename to full name (`@Qualifier("org.egov.…")`), or force a short name at declaration with `@Service("simpleName")` |
| Programmatic `context.getBean("simpleName", …)` calls | Breaks — same reason | Use `getBean(Type.class)` (type-based) or the full name |
| Interface-typed autowiring with multiple impls | Not fixed — ambiguity is at the interface level, not name | `@Primary`, `@Qualifier`, or `@ConditionalOnProperty` |
| Two `@Bean` methods with the same method name in two `@Configuration` classes | Not fixed — FQN generator only affects component scans, not `@Bean` names | Rename the method, or add `name = "..."` on one of the `@Bean` annotations |
| `@ConfigurationProperties(prefix = "same.prefix")` in two services | Not fixed — bindings silently overwrite | Namespace the prefix (`app.service-a.security` vs `app.service-b.security`) |

**How each service knows which bean to use in practice.** The Java `import` statement is already the disambiguator. `import org.egov.user.service.NotificationService` at the top of a file declares the type of every `NotificationService` reference below. Spring autowires by *type* first, name only as a tiebreaker — and because different packages produce different Java types, ambiguity never arises at type-based injection sites. FQN naming is essentially about making the Spring container agree with what Java already knew.

---

## 7. Runtime — What Happens on One Request

Both modes use the same code path. The only difference is what `mdms.service.host` resolves to.

**Standalone idgen** → remote HTTP:

```
curl POST http://localhost:8088/egov-idgen/id/_generate
  → IdGenerationController → IdGenerationService → MdmsService
  → mdmsClientService.getMaster()                      // MdmsClientService (RestTemplate)
  → RestTemplate.postForObject("http://:8094/egov-mdms-service/v1/_search")
  → separate JVM handles the request, returns MdmsResponse over the wire
```

**Monolith** → loopback HTTP:

```
curl POST http://localhost:8080/egov-idgen/id/_generate
  → IdGenerationController → IdGenerationService → MdmsService                     [Tomcat exec-1]
  → mdmsClientService.getMaster()                      // same MdmsClientService
  → RestTemplate.postForObject("http://localhost:8080/egov-mdms-service/v1/_search")
  → same JVM's Tomcat picks the request up on a different worker thread            [Tomcat exec-2]
  → MDMSController → MDMSService.searchMaster(...)  → MdmsResponse
  → response goes back over the socket to exec-1, which returns to the caller
```

Same JVM, but two Tomcat threads, one socket connection, one full HTTP serialization each way. Kernel-loopback so no actual network hop, but semantically identical to the remote case — the callee has zero shared memory with the caller. Traceable via a single traceId that spans both worker threads.

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

# 1. shared library (MdmsClientService — the RestTemplate-based HTTP client)
cd libraries/mdms-client && mvn -q clean install -DskipTests && cd -

# 2. mdms-service (the MDMS HTTP endpoint + master-data loader)
cd egov-mdms-service && mvn -q clean install -DskipTests && cd -

# 3. idgen (autowires MdmsClientService, DB layer)
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
Flyway ... Successfully validated 58 migrations
Started PtIdGenerationApplication in ~7 seconds
Tomcat started on port 8088 (http) with context path '/'
```

Requests fire an `MdmsClientService` log line showing the outbound call to `http://localhost:8094/egov-mdms-service/v1/_search`. Same class, same log line format, in both standalone and monolith — only `mdms.service.host` differs.

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
Flyway ... Successfully validated 58 migrations
Reading files from: /Users/aniket/Documents/egov-mdms-data/data/pb
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

**Expected**: `HTTP 200`, body `{"idResponses":[{"id":"PT-YYYY-MM-DD-000NNN"}]}`. Under the hood: `IdGenerationService` → idgen's `MdmsService` → `MdmsClientService.getMaster()` → `RestTemplate.postForObject("http://localhost:8080/egov-mdms-service/v1/_search")` → the same JVM's Tomcat routes to `MDMSController` → `MDMSService.searchMaster()` on a different worker thread → response comes back over the socket.

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

**Expected**: `HTTP 200`. Both endpoints served by the same Tomcat, but the idgen call under the hood makes an outbound loopback HTTP call to reach MDMS on the same port.

**Verify the loopback via traceId**:

```bash
grep -E "MdmsClientService|Received request URI.*_search|Response code sent" /tmp/monolith-log
```

You'll see one traceId spanning three log lines: `MdmsClientService` (idgen's outbound POST log) on thread `080-exec-1`, `Received request URI: .../egov-mdms-service/v1/_search` (Tomcat catching its own emission) on `080-exec-2`, then two `Response code sent: 200` — one from the MDMS handler, one from the idgen handler. Port 8094 is not listening in this mode; the whole exchange happens on `:8080` between two threads of the same JVM's servlet container.

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
| **mdms standalone** | `cd egov-mdms-service && mvn spring-boot:run` | `POST http://localhost:8094/egov-mdms-service/v1/_search` | HTTP 200 with `MdmsRes` payload |
| **idgen standalone** (mdms up on :8094) | `cd egov-idgen && mvn spring-boot:run` | `POST http://localhost:8088/egov-idgen/id/_generate` | HTTP 200 → new `BILL-000NNN`. `MdmsClientService` calls `http://localhost:8094/…` |
| **Monolith** | `mvn digit-bundler:generate -Dbundle=idgen-monolith` &nbsp;→&nbsp; `mvn -pl bundles/idgen-monolith spring-boot:run` | `POST http://localhost:8080/egov-idgen/id/_generate` | HTTP 200 → new `BILL-000NNN`. `MdmsClientService` calls `http://localhost:8080/…` (loopback); one traceId spans two Tomcat worker threads. |

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
| `libraries/mdms-client/.../MdmsClientService.java` | Unchanged from original codebase | Sole MDMS client (`RestTemplate`), used in both modes |
| `libraries/mdms-client/pom.xml` | Drop pinned `<lombok.version>1.18.22</lombok.version>` so Boot 3.4.5 manages it (1.18.38); add explicit `annotationProcessorPaths` for lombok | Old Lombok breaks on newer JDK 17 patches; explicit AP path is reliable |
| `egov-mdms-service/.../MDMSController.java` | `@RequestMapping("/egov-mdms-service/v1")` at class level | Path ownership moved off servlet context |
| `egov-mdms-service/pom.xml` | Bump Spring Boot 3.2.2 → 3.4.5; add `classifier=exec`; exclude `application.properties` from jar; add annotationProcessorPaths for lombok | Unified SB version; plain jar for bundle deps; no property conflict |
| `egov-mdms-service/service.yaml` | **NEW** manifest — just scan-packages + defaults-file (no capabilities) | Metadata the bundler needs to compose services |
| `egov-idgen/.../IdGenerationController.java` | `@RequestMapping("/egov-idgen/id/")` at class level | Same reason as MDMSController |
| `egov-idgen/pom.xml` | Add `flyway-database-postgresql`; add `classifier=exec`; exclude `application.properties`; drop old lombok pin; add annotationProcessorPaths | Managed Flyway 10.x needs postgres extension; same jar contract; lombok fixes |
| `egov-idgen/service.yaml` | **NEW** manifest — just scan-packages + defaults-file | Same as mdms |
| `<each>-defaults.properties` | **NEW** — real config split out from `application.properties` | Reusable in both modes without conflict |
| Both `application.properties` | Now thin: `spring.config.import` + `server.port` | Standalone bootstrap glue only; excluded from published jar |
| `package.yaml` | **NEW** bundle manifest | Composition decision; carries the monolith's `mdms.service.host=http://localhost:8080/` loopback override |
| `tools/bundler/pom.xml`, `.../GenerateMojo.java` | **NEW** maven plugin | Reads manifests, emits bundle module. Emits: pom, main class (FQN-safe `@ComponentScan`), `application.properties`, `application-<profile>.properties` |
| `.gitignore` | Add `bundles/` | Generated content, not committed |
| Root `pom.xml` | Register `tools/bundler` module | Plugin builds with the rest of the reactor |
| Kafka demo (`Idgen`/`MdmsDummyListener`) | **NEW** in each service under `.../dummy/` | `@KafkaListener` with explicit `groupId=` per service, gated by `egov.kafka.demo=true` |

---

## 11. Known Gaps / Not in Scope

- **Kafka / OTEL** wiring in monolith mode. OTEL config surfaces two legitimate conflicts today (service name, ignore-urls); resolving them in `package.yaml` is a one-minute follow-up.
- **Fat-jar packaging** for shipping the monolith as a container. `spring-boot-maven-plugin` default works but has not been size-tuned.
- **Auto-discovery** of new services from filesystem — bundler currently requires each service to have a hand-written `service.yaml`.
- **Third-service inclusion**. Pattern is proven with two services; extending to (say) `egov-user`, `egov-workflow-v2` requires: bump Spring Boot to 3.4.5, add controller `@RequestMapping`, add `service.yaml`, and split property files — same recipe.
- **Same-JVM property collisions.** Truly shared infra keys (e.g., `otel.service.name`, `server.port`, `spring.datasource.url`) can have only one value per JVM in a monolith. The bundler flags conflicts; humans must decide the value.
- **Bean-name collisions across services (handled).** Every bundle generated by the plugin uses `FullyQualifiedAnnotationBeanNameGenerator` plus a `@ComponentScan.Filter` excluding all `@SpringBootConfiguration` classes on the classpath — see Section 6.3 for the mechanism and the failure modes (`@Bean` method-name clashes, `@Qualifier` string references, shared `@ConfigurationProperties` prefixes) that FQN naming *doesn't* cover. Those remaining cases are Spring-context issues, not bundler concerns; developers apply the standard fixes (rename `@Bean`, drop or update `@Qualifier`, namespace the properties prefix).

    A future bundler enhancement could go a step further and *statically* scan each included service's classes at `generate` time to warn about duplicate `@Service` / `@Component` / `@Bean` names — surfacing the risk at build time before it becomes a runtime surprise. Mirrors the property-conflict warning already implemented. Not built yet.

---

## 12. What This PoC Proves

1. Two services can share a JVM without source-forking and without any Java-level indirection — same client class in both modes, only the target URL changes.
2. Standalone and monolith deployments coexist off the same codebase — no separate branches, no shadow builds.
3. Composition can be **dynamic**: `mvn digit-bundler:generate` produces the bundle on demand from `package.yaml`, so new bundles are declarative edits, not new modules.
4. The bundler surfaces cross-service property clashes at generate-time so silent misconfiguration ("whichever service's default was loaded last wins") becomes a visible `[WARNING]` before the monolith ever boots.

The design is ready to scale to more services with the same recipe.
