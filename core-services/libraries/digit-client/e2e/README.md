# digit-client live tests

Calls every SDK client against the real services. This is the layer no mocked test can cover: that
the URL a client builds is one the service actually serves, that the headers the interceptor attaches
are the ones it requires, and that a real response body deserializes into the SDK's model without
throwing — the exact class of defect the esMagico gap report was full of.

## Quick start

```bash
./port-forward.sh start          # one local port per service
./port-forward.sh check          # confirm each answers on its context path
cp .env.local.example .env.local # then paste a fresh TEST3 token (gitignored)

./run-live-tests.sh              # pod-direct
./run-live-tests.sh gateway      # through Kong, with the token
./run-live-tests.sh pod LiveReadPathTest#account_listsTenantsAndConfigs
./port-forward.sh stop
```

`mvn test` never runs these — they carry `@Tag("live")`, which the pom excludes by default and the
`live` profile re-includes. A normal build stays hermetic and offline.

## The two modes prove different things

| | `pod` (default) | `gateway` |
|---|---|---|
| Base URL | `localhost:1800x` per service | one origin, Kong routes by context path |
| Auth | none needed — services accept tenant headers | token required |
| Isolates | the SDK's own request/response contracts | routing, `Authorization` propagation, JWT decode |

Pod-direct is the default because nothing sits between client and service: a failure is in one of
those two and never in a proxy. Gateway mode is the only place `headerPropagationIsWhatMakesAuthenticatedCallsWork`
can run, because it asserts the *negative* — drop the context and the same call must come back 401.
Pod-direct would pass that for the wrong reason, since the token is checked at Kong.

## Configuration

`services.properties` is the single source of truth for ports, cluster service names and context
paths, read by both `port-forward.sh` and `LiveEnv`, so a tunnel and the client that dials it cannot
drift apart. Ports sit in an 18000 block, deliberately clear of the SDK's own defaults
(8080/8085/8091/8100/8999) — if a base-url is ever left unset, the call fails loudly instead of
quietly reaching whatever runs on a default port.

Credentials come from `.env.local` (gitignored) via the environment. No token is ever committed, and
nothing here prints one.

## What was verified against the cluster

- **All 12 services reachable**, pod-direct and through the gateway. The CrashLoopBackOff `otp` pod
  is the legacy service; the SDK targets `otp-java`, which is healthy.
- **Context paths match what the SDK hardcodes** — `/accounts`, `/employee`, `/individuals`,
  `/boundary`, `/billing`, `/registry`, `/idgen`, `/notification`, `/otp`, `/workflow`, `/filestore`,
  `/mdms-v2`. This is the live evidence for treating a context path as a platform constant.
- **Auth is terminated at Kong.** Pod-direct calls need no token, only `x-tenant-id` (plus
  `x-user-id` for writes). Through the gateway, `/employee` and `/registry` answer 401 without one
  while `/accounts/v3/tenants` is public.
- **MDMS requires `X-Client-Id`.** `MdmsClient` refuses to call without it. In a servlet request the
  SDK derives it from the token, so `LiveEnv` does the same via `JwtTokenUtil.extractClientId`, which
  is also that method's only live coverage.

## Two things worth a closer look

- `JwtTokenUtil.extractClientId` returns the token's **`sub`** claim, which is the user id — so
  `X-Client-Id` and `X-User-Id` carry the same value. Works, but `azp` is the claim that actually
  identifies a client.
- `notification` answers **500** on `/notification/actuator/health` while its API serves fine. That
  is why `LiveEnv.reachable` treats any HTTP response as reachable rather than gating on 2xx.

## Not covered here

Write paths. They belong in their own suite, where created data can be named per run and cleaned up —
and where the idgen/`individualid` collision recorded during the employee work needs care.

Nor does this prove the **published artifact** works: these tests run against `target/classes`, so
they cannot show that a consumer resolving the jar from Nexus gets the client beans auto-configured,
the `digit.services.*.base-url` properties bound, and headers propagated from a real inbound servlet
request. That needs a small Boot app depending on the published snapshot — the next phase.
