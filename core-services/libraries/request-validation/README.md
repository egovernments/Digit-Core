# Request validation

Reusable Java 8+ / Spring MVC library implementing the request-content policy in
[the design](../../../docs/request-validation-design.md). It inspects wire JSON
before DTO binding and scalar request text before conversion, without changing
accepted bytes. It is a secondary input control, not a guarantee against XSS.
Output encoding and HTML sanitization at rendering boundaries remain necessary.

## Adoption

No service is automatically annotated by this change. The gateway is unaffected.

**One dependency for every service**, whatever its Java and Spring Boot version:

```xml
<dependency>
  <groupId>org.egov.services</groupId>
  <artifactId>request-validation</artifactId>
  <version>1.0.1-SNAPSHOT</version>
</dependency>
```

| Service stack | Supported |
| --- | --- |
| Java 8, Spring Boot 1.5 (e.g. `egov-user`), tracer 2.1.x | yes |
| Java 8+, Spring Boot 2.x, tracer 2.1.x | yes |
| Java 17, Spring Boot 3.x, tracer 2.9.x | yes |

The jar is Java 8 bytecode, compiled against the oldest supported API (Boot 1.5 /
Spring 4.3), which later Spring versions still provide. The only code that
differs between `javax.servlet` (Boot 1.5/2.x) and `jakarta.servlet` (Boot 3.x)
is `ServletSupport`. Both implementations are in the jar, and the one matching the
running Spring MVC is picked at startup. The auto-configuration is registered
for every Boot version (`spring.factories` and `AutoConfiguration.imports`).
Spring, Boot, servlet API, slf4j and tracer come from the service; jackson-core
2.18 is shaded and relocated inside the jar (the parser limits need it), so the
service's own Jackson version is not changed. The only transitive dependency is
jsoup.

Every setting can be given in `application.properties`/`.yml` or as an environment
variable, on every supported Boot version. Environment variables are the property name
in upper case with dots and dashes as underscores, for example
`EGOV_REQUEST_VALIDATION_ENABLED`, `EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT`,
`EGOV_REQUEST_VALIDATION_MODE` and `EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH`. They
override the file, and Spring resolves placeholders in them and converts them as it does
for any property. On Boot 1.5 this holds where the file writes the key as in this README
(kebab case, lists comma-separated): Boot 1.5 lets a camelCase name (`limits.maxDepth`) or
a YAML list in the file win, and ignores a camelCase prefix (`egov.requestValidation`)
altogether. Leave a variable out rather than empty: a blank `mode` or `activation` keeps
the default and an empty list value is an empty list, but Boot 2.7/3 reject a blank number
or true/false value at startup.
(Boot 1.5 cannot bind such a variable for a setting no other source mentions, and Boot
2.0-2.6 cannot for a `limits`, `rules` or `log` setting when no other source mentions that
group; on javax.servlet hosts the library declares those settings for Spring to bind,
before the application beans are created.) Use this form: Boot 2.x/3.x also accept
`EGOV_REQUESTVALIDATION_*`, but Boot 1.5 ignores it. The list properties
`rules.disallowed-controls`, `rules.denied-schemes` and `rules.denied-data-media-types`
take a comma-separated value or a YAML list, and a configured list replaces the defaults
(an empty list denies nothing). The defaults need no configuration.

The dependency alone does nothing: validation starts only with `enabled: true`.
Use a fixed release version for production after validation and release.
The host service must provide tracer and its error advice (`CustomException` → HTTP 400).

Explicitly configure the structured-body default, then opt in controllers:

```yaml
egov:
  request-validation:
    enabled: true
    structured-default: true
    activation: ANNOTATED
    mode: REPORT
```

```java
@RestController
@ValidateRequest
class ExampleController {
    @PostMapping("/_create")
    public Object create(@RequestBody CreateRequest request) { /* service call */ }
}
```

`REPORT` only observes. Content findings, malformed JSON, incompatible charsets,
duplicate properties, ambiguous RequestInfo spellings, and resource limits are
logged, and the original bytes go to the host's converter, which accepts or
rejects them as it would without this library. When a finding stops inspection,
one `inspection-incomplete` event is logged per request (the last of the ten
log slots is kept for it). A body whose declared length exceeds the limit is
passed on unread; an undeclared oversized body is read to the limit plus one
byte and replayed, followed by the unread rest of the same stream. A transport
I/O failure propagates as it would without the library. A scalar limit stops
the remaining scalar checks; the body is still inspected. Set `mode: ENFORCE`
to reject all of these after calibration.

Override an individual body using `@ValidateRequest(structured = DISABLED,
reason = "...")` on its `@RequestBody` or `HttpEntity` argument. This skips the
entire body, including RequestInfo. Query/form/path checks still run. Prefer
`skipPaths = {"/Mdms/data/help", "/items/*/description"}` for intentional
markup-bearing fields; keep output sanitization for these exclusions. An empty
skip path `""` (the whole document) fails startup: exclude a whole body with
`structured = DISABLED` and a reason. `"/"` is the property with an empty name.

Class/method annotations activate handlers. A parameter annotation refines an
already active handler; it does not activate one by itself. In `activation: ALL`,
all MVC handlers are active except explicit class/method `enabled = false`.
Class/method `enabled` never inherits from a parameter. Parameter → method →
class → configuration precedence applies to body mode, structured inspection,
and limits. Skip paths are combined. Missing exclusion reasons produce startup
warnings. Startup also lists effective handler/body coverage.

## Configuration

All keys below use the `egov.request-validation` prefix.

| Key | Default | Meaning |
| --- | --- | --- |
| `enabled` | `false` | Opt-in switch; unless `true`, no validation infrastructure is created |
| `structured-default` | required when enabled | Explicit `true` or `false`; missing value fails startup |
| `activation` | `ANNOTATED` | `ANNOTATED` or `ALL` |
| `mode` | `REPORT` | `REPORT` or `ENFORCE` |
| `inspect-content-type-header` | `true` | Also inspect this header as scalar text |
| `reject-duplicate-keys` | `true` | Reject duplicate property names |
| `reject-dual-request-info` | `true` | Reject both root `RequestInfo` and `requestInfo` |
| `log.report-sample-rate` | `1.0` | REPORT event sampling, 0–1 |
| `limits.max-body-bytes` | `10485760` | Bounded body buffer, plus one byte to detect overflow |
| `limits.max-depth` | `64` | Maximum JSON nesting depth; hard ceiling 256 |
| `limits.max-string-length` | `1000000` | Parsed string characters |
| `limits.max-name-length` | `256` | Parsed property-name characters |
| `limits.max-tokens` | `2000000` | JSON token / scalar item budget |
| `limits.max-number-length` | `1000` | Number token characters |
| `limits.max-scalar-length` | `65536` | Each scalar name or value |
| `rules.markup-start` | `true` | R1: markup-starting characters |
| `rules.url-scheme` | `true` | R2: denied leading URL schemes/media types |
| `rules.event-handler` | `true` | R3: listed event attribute followed by `=` |
| `rules.disallowed-controls` | `[0]` | R4: denied C0 controls except TAB/LF/CR; NUL by default |
| `rules.denied-schemes` | `[javascript, vbscript]` | Case-insensitive scheme names |
| `rules.denied-data-media-types` | HTML, XHTML, SVG | Denied `data:` media types |
| `rules.decode-rounds` | `2` | Composed HTML/percent/NFKC variants; 0–3 |
| `rules.normalize-nfkc` | `false` | Optional normalization for detection only |

Annotation limit attributes use camelCase and default to `-1` (inherit).
When enabled, NFKC also runs with `decode-rounds: 0`; zero rounds disables
entity/percent decoding, not the separate normalization option.
Limits must be positive; invalid service or annotated-handler configuration
fails startup. The cumulative scalar text per request is additionally bounded
by `max-body-bytes` **characters**, and scalar items by `max-tokens`. These bounds
limit library work; servlet/container parsing may already have happened.

Skip paths use JSON Pointer escaping (`~0` for `~`, `~1` for `/`). `*` matches one
whole segment, not an arbitrary suffix. Matching is case-sensitive and follows
wire names, including array indices. A match skips content checks for that
subtree, including its property name, while retaining syntax and resource checks.

## Coverage and errors

JSON `@RequestBody` and `HttpEntity` parameters support `application/json` and
`application/*+json`, including UTF-8 charset parameters. Other media types,
headers except Content-Type, cookies, multipart files, WebFlux handlers, and
non-HTTP entry paths are outside coverage. An absent/optional body follows
Spring's empty-body handling. JSON inspection requires UTF-8; incompatible
declared charsets and invalid UTF-8 bytes are rejected.
Failures before a handler is selected (including mapping/media-type failures)
occur before these hooks and require safe error handling in the host service.

The inspector walks names and strings at every depth, including arrays, unknown
properties, and strings sent where the DTO expects a numeric field. It does not
parse JSON serialized inside a string as another document. Bounded decoding is
for detection only. Legitimate HTML, angle-bracket email notation, and prose
containing event assignments may violate the policy; calibrate with real data.

Rejections extend tracer's `CustomException` with one fixed code/message:

- `REQUEST_CONTENT_NOT_ALLOWED`
- `REQUEST_JSON_MALFORMED`
- `REQUEST_LIMIT_EXCEEDED`
- `REQUEST_JSON_DUPLICATE_KEY`

The intended tracer response is HTTP 400 with `Errors[{code,message}]`. The
library omits locations from client errors and never retains parser exceptions.
Logs contain safe locations, rule IDs and lengths, with at most ten observations
per request. They contain neither input values nor decoded variants. The logger
temporarily bounds/sanitizes MDC values for its own messages.

Each adopter must verify its own exception-advice precedence. Existing tracer
body logging/error publishing and its unbounded upstream body cache are outside
this library's control. Library limits do not establish an end-to-end ingress
limit. Gateways that reject before routing are also outside its coverage.

## Registration and verification

Boot loads the auto-configuration through `AutoConfiguration.imports` (Boot 2.7+)
or `spring.factories` (Boot 1.5–2.6); its package is outside both `org.egov` and `digit`. Infrastructure has no component/advice
stereotypes. A bean post-processor attaches body advice before MVC adapter
initialization, preserving pre-existing advice; a dedicated MVC configurer adds
the scalar interceptor. Custom MVC argument resolvers or custom body advice need
adopter review, because they may change the normal conversion path.

Unit and MVC test sources cover content policy, unknown fields, wrong types,
byte preservation, exclusions, limits, and registration.

Build with JDK 17+ (the jakarta part of `ServletSupport`, in `src/jakarta/java`,
is compiled against Spring 6; it is still Java 8 bytecode). `mvn install` runs the
stack-independent unit tests. `compatibility/` is a test harness only (nothing is
published from it): it uses the installed jar as a service would and runs the
unit tests plus the MVC integration test on each stack:

```sh
compatibility/run-all.sh          # everything; set JAVA8_HOME / JAVA17_HOME if needed
# or one stack:
mvn install
JAVA_HOME=<jdk8>  mvn -f compatibility/pom.xml -Pboot-1.5 clean test   # Boot 1.5 (egov-user)
JAVA_HOME=<jdk8>  mvn -f compatibility/pom.xml -Pboot-2.2 clean test
JAVA_HOME=<jdk17> mvn -f compatibility/pom.xml -Pboot-3.2 clean test
JAVA_HOME=<jdk17> mvn -f compatibility/pom.xml -Pboot-3.4 clean test
```

`compatibility/src/boot2plus` holds the MVC test for Boot 2.2+; `compatibility/src/boot1`
holds the same scenarios written with Boot 1.5 test support. Keep them in step.
Keep `src/main/java` free of Java 9+ APIs and of Spring 5+-only APIs (the code is
compiled with `--release 8` against Spring 4.3, so the build fails if it is not),
and keep servlet-API use inside `ServletSupport` implementations.

Neither a successful compile nor these local fixtures verifies
an adopted/deployed service or its latency. Measure largest legitimate payloads
before ENFORCE rollout. The gateway rollback decision remains separate.

References: [Spring auto-configuration](https://docs.spring.io/spring-boot/3.4/reference/features/developing-auto-configuration.html),
[OWASP input validation](https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html).
