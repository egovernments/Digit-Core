# Gateway Kubernetes Discovery

Init container that discovers Kubernetes services annotated for gateway routing and generates a
Spring Cloud Gateway routes properties file. The gateway container mounts the same volume and loads
the file at startup (`SPRING_ROUTES_FILEPATH=file:/etc/zuul/routes.properties`).

## How it works

1. Connects to the Kubernetes API using the pod's service account (`rest.InClusterConfig`).
2. Lists services in the configured namespaces and filters those carrying the `zuul/route-path` annotation.
3. Builds one Spring Cloud Gateway route per matching service.
4. Sorts routes so that, for the same path, host-restricted routes come before catch-all (host-less) routes.
5. Writes the routes file to `OUTPUT_FILE_PATH`.
6. Validates the final route list for unreachable (shadowed) routes and warns or fails per `ROUTE_VALIDATION_MODE`.

## Environment variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `OUTPUT_FILE_PATH` | yes | — | File path to write the generated routes properties (e.g. `/etc/zuul/routes.properties`). The process panics if unset. |
| `NAMESPACE` | no | cluster-wide | Comma-separated list of namespaces to scan (e.g. `egov,chad-prod,kebbi-azm`). Unset scans all namespaces. |
| `DEFAULT_INTERNAL_GATEWAY_SERVICE` | no | — | Fallback target used for services annotated `internal-gateway-enabled: "true"` that do not specify `internal-gateway-service`. When unset, such services keep their direct service URL. |
| `ROUTE_VALIDATION_MODE` | no | `warn` | `warn` logs shadowed routes and exits 0; `strict` logs them and exits non-zero, failing the init container. Unknown values fall back to `warn`. |

## Service annotations

Annotations are read from each Kubernetes `Service` object. Port is always taken from the first
service port (`spec.ports[0].port`).

| Annotation | Effect |
|---|---|
| `zuul/route-path` | Opt-in. Route path context; generates predicate `Path=/<value>/**`. Route id is `<path>-<namespace>`. |
| `zuul/route-host` | Adds predicate `Host=<value>`. Accepts exact hosts, wildcards (`*.example.org`) and comma-separated lists, same semantics as the Spring Cloud Gateway `Host` predicate. |
| `gateway-keyResolver` | Enables `RequestRateLimiter` filter and sets `key-resolver: "#{<value>}"`. |
| `gateway-replenishRate` | Enables `RequestRateLimiter` filter and sets `redis-rate-limiter.replenishRate`. |
| `gateway-burstCapacity` | Enables `RequestRateLimiter` filter and sets `redis-rate-limiter.burstCapacity`. |
| `internal-gateway-enabled` | When `"true"`, the route URI targets the internal gateway instead of the service itself. |
| `internal-gateway-service` | Overrides the internal gateway target for this service; falls back to `DEFAULT_INTERNAL_GATEWAY_SERVICE`. |

### Annotated service example

```yaml
apiVersion: v1
kind: Service
metadata:
  name: egov-mdms
  namespace: egov
  annotations:
    zuul/route-path: "egov-mdms-service"
    zuul/route-host: "api.egov.org"
    gateway-replenishRate: "20"
    gateway-burstCapacity: "40"
spec:
  ports:
    - port: 8080
```

Generated output:

```properties
spring.cloud.gateway.routes[0].id=egov-mdms-service-egov
spring.cloud.gateway.routes[0].uri=http://egov-mdms.egov:8080/
spring.cloud.gateway.routes[0].predicates[0]=Path=/egov-mdms-service/**
spring.cloud.gateway.routes[0].predicates[1]=Host=api.egov.org
spring.cloud.gateway.routes[0].filters[0].name=RequestRateLimiter
spring.cloud.gateway.routes[0].filters[0].args.redis-rate-limiter.replenishRate=20
spring.cloud.gateway.routes[0].filters[0].args.redis-rate-limiter.burstCapacity=40
```

### Internal gateway example

```yaml
metadata:
  name: pgr-services
  namespace: chad-prod
  annotations:
    zuul/route-path: "pgr-services"
    internal-gateway-enabled: "true"
    internal-gateway-service: "internal-gateway-chad.chad-prod"
```

Generated URI: `http://internal-gateway-chad.chad-prod:8080/` (port still from the annotated service).
Without `internal-gateway-service`, the URI uses `DEFAULT_INTERNAL_GATEWAY_SERVICE`.

## Route ordering

Routes are sorted by path; within the same path, host-restricted routes are emitted before host-less
routes. This guarantees a tenant-specific route (`Path=/user/** Host=chad.example.org`) is matched
before the catch-all (`Path=/user/**`).

## Route validation (shadowed routes)

A route is unreachable when an earlier route matches every request it could match, so it can never
receive traffic. Detection compares each route against all earlier routes:

- Path covers: identical path, or segment prefix (`/user/**` covers `/user/v2/**`, but not `/user2/**`).
- Host covers: earlier route has no host (catch-all), hosts match exactly, a wildcard covers the host
  (`*.example.org` covers `api.example.org`), or a comma list is a superset (`a.org,b.org` covers `b.org`).

Examples that are flagged:

| Earlier route | Later route | Reason |
|---|---|---|
| `Path=/user/**` | `Path=/user/**` (another namespace) | duplicate path, both host-less |
| `Path=/user/**` | `Path=/user/v2/**` | prefix shadowing |
| `Path=/x/** Host=*.example.org` | `Path=/x/** Host=api.example.org` | wildcard covers exact host |
| `Path=/y/** Host=a.org,b.org` | `Path=/y/** Host=b.org` | host list superset |

Log output:

```
UNREACHABLE ROUTE id=user-chad-prod (index 4) shadowed by id=user-egov (index 3): every request matching Path=/user/** Host="" is consumed first by Path=/user/** Host=""
Route validation result: 1/12 routes unreachable
```

In `strict` mode the process then exits non-zero (the routes file is still written first, so it stays
inspectable on the failed pod). In `warn` mode the gateway starts normally.

## Deployment

Runs as an init container sharing an `emptyDir` volume with the gateway:

```yaml
initContainers:
  - name: gateway-kubernetes-discovery
    image: egovio/gateway-kubernetes-discovery:<tag>
    env:
      - name: OUTPUT_FILE_PATH
        value: /etc/zuul/routes.properties
      - name: NAMESPACE
        value: egov,chad-prod
      - name: ROUTE_VALIDATION_MODE
        value: warn
    volumeMounts:
      - name: zuul-routes
        mountPath: /etc/zuul
volumes:
  - name: zuul-routes
    emptyDir: {}
```

- The service account needs `list` permission on `services` in every scanned namespace.
- The image is `FROM scratch` and runs as non-root UID `10001`; the output volume must be writable by
  that UID (`emptyDir` is, other volume types may need `fsGroup`).

## Build

Multi-arch (amd64/arm64) build from the repository root; `WORK_DIR` points at this module:

```bash
docker build \
  -f accelerators/gateway-kubernetes-discovery/Dockerfile \
  --build-arg WORK_DIR=accelerators/gateway-kubernetes-discovery \
  -t egovio/gateway-kubernetes-discovery:local .
```

CI: GitHub Action "Build Pipeline" with `pipeline_name=gateway-kubernetes-discovery`
(resolved via `build/build-config.yml`).

Local development:

```bash
cd accelerators/gateway-kubernetes-discovery
go build
```
