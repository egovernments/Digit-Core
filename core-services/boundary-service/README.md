# Boundary Service

Spring Boot service that manages a tenant's administrative hierarchy — boundary **entities**, boundary **hierarchy definitions**, and the boundary **relationships** that record, for each boundary, its type, parent, and materialized path within a hierarchy (country → province → district → … → village).

- **Java:** 17
- **Framework:** Spring Boot 3.2.2
- **Datastore:** PostgreSQL (Flyway migrations under `src/main/resources/db/migration/main`)
- **Messaging:** Kafka. Every write (entity/hierarchy/relationship create & update, single **and** bulk) is published to a topic and written to PostgreSQL by `egov-persister`. boundary-service itself performs **no direct DB write** — it only reads (for validation/search) and publishes. There is no Kafka consumer in this service.
- **Context path:** `/boundary-service` &nbsp;•&nbsp; **Default port:** `8081`

---

## API surface

All endpoints are `POST`. Base URL: `http://<host>:8081/boundary-service`.

| Resource | Path | Purpose | Write model |
| --- | --- | --- | --- |
| Hierarchy definition | `/boundary-hierarchy-definition/_create` | Define the boundary-type order for a hierarchy | async (Kafka → persister) |
| Hierarchy definition | `/boundary-hierarchy-definition/_search` | Search hierarchy definitions | — |
| Boundary entity | `/boundary/_create` | Create boundary entities (geometry) | async (Kafka → persister) |
| Boundary entity | `/boundary/_search` | Search boundary entities | — |
| Boundary entity | `/boundary/_update` | Update boundary entities | async (Kafka → persister) |
| Boundary relationship | `/boundary-relationships/_create` | Create a **single** relationship | validate → publish → `202 Accepted` |
| Boundary relationship | **`/boundary-relationships/bulk/_create`** | **Create relationships in bulk** | validate+enrich **synchronously**, publish → `200 OK` |
| Boundary relationship | `/boundary-relationships/_search` | Search the relationship tree | — |
| Boundary relationship | `/boundary-relationships/_update` | Update a relationship's parent | async (Kafka → persister) |

### Single vs. bulk relationship create

Both paths validate + enrich a relationship (assign `id`, audit details, and the ancestral materialized path) and then **publish it to the `save-boundary-relationship` topic**, which `egov-persister` writes with an idempotent `INSERT … ON CONFLICT (tenantId, code, hierarchyType) DO NOTHING`. Neither path writes to the database directly.

- The **single** `_create` validates one record and publishes it, returning `202 Accepted` (acceptance, not a committed write).
- The **bulk** `_create` validates + enriches every record in the request **synchronously**, publishes the valid ones (one message per record), and returns `200 OK` with a **per-record outcome** so the caller learns immediately which records were accepted and which failed validation, and with what reason.

See [`docs/Bulk-Boundary-Relationship-Creation-Design.docx`](docs/Bulk-Boundary-Relationship-Creation-Design.docx) for the design rationale and [`docs/Bulk-Boundary-Relationship-Flow.docx`](docs/Bulk-Boundary-Relationship-Flow.docx) for the end-to-end flow.

---

## Bulk Boundary Relationship Creation

`POST /boundary-relationships/bulk/_create`

Creates many relationships in one request with a **per-record outcome**. Validation and enrichment happen synchronously on the request thread **before anything is placed on Kafka**; each valid record is then published to `save-boundary-relationship` for `egov-persister` to write. Individual record failures do **not** fail the whole request.

### Semantics

- **Request guards.** `RequestInfo.userInfo` must be present, and the request must carry `1 … boundary.bulk.max.size` (default `100`) relationships. These are enforced in-service (bean validation is not active in this deployment) and return a structured `400` (`BULK_REQUEST_INFO_MISSING` / `BULK_REQUEST_EMPTY` / `BULK_REQUEST_SIZE_EXCEEDED`).
- **Per-record validation.** Each record runs the same business rules as the single create (boundary entity exists, no duplicate, parent exists, correct hierarchy level; `code`/`tenantId`/`hierarchyType` must not contain the reserved `|` path delimiter). A record that fails validation is reported in `failedBoundaryRelationships`; the rest continue.
- **Persistence via egov-persister.** Validated + enriched records are published, **one message per record**, to `save-boundary-relationship`. The publish is blocking (it returns once the broker has accepted each record). `egov-persister` writes each with `INSERT … ON CONFLICT (tenantId, code, hierarchyType) DO NOTHING`, so redelivery / re-submission is a safe no-op and one un-insertable record never fails the others. A record accepted here is reported in `successfulBoundaryRelationships` ("accepted for persistence", not "already committed").
- **Transient failures are retryable, not fatal.** A parent/entity not yet persisted, a transient DB error, or a publish failure (broker unreachable within `max.block.ms`) is reported with a retryable code (`PARENT_NOT_FOUND`, `BOUNDARY_ENTITY_DOES_NOT_EXIST`, `BULK_RELATIONSHIP_PERSIST_TRANSIENT`) rather than aborting; the caller retries only those records.
- **Intra-request de-duplication.** Two records with the same `(tenantId, hierarchyType, code)` in one request are rejected (`DUPLICATE_RECORD_IN_REQUEST`).

### Caller contract

The endpoint expects each batch to be a set of **siblings whose parent is already persisted**:

1. Create the hierarchy **top-down**, one level at a time. Confirm an upper (single-create) level is persisted before creating its children.
2. For high-cardinality lower levels (locality, village), split into batches of ≤ `boundary.bulk.max.size` (100) where every record in a batch shares one parent, and submit them with bounded parallelism.
3. Treat the response per record: `DUPLICATE_RECORD` is an idempotent success; the retryable codes above should be retried (a bulk-level parent may still be committing — the persister writes asynchronously); any other code is a permanent data error. Re-submission targets only the missing records and is idempotent.

### Scaling & reliability

- **Horizontal scale** is by running more boundary-service replicas behind the load balancer (a stateless HTTP path) and by the caller submitting chunks concurrently — there is no Kafka consumer group to size and no per-partition head-of-line blocking.
- **Idempotency** comes from `INSERT … ON CONFLICT (tenantid, code, hierarchytype) DO NOTHING`, so a parent that is briefly late simply causes its children to be retried by the caller until it lands; ordering across levels is *not* required.
- **DB-write reliability** (per-record isolation of an un-insertable row, transient-DB retry, dead-lettering) is owned by `egov-persister`. Because it is one message per record, a bad record is isolated on its own regardless of whether the persister runs its normal listener or the optional batch listener.

### Request

```json
{
  "RequestInfo": { "apiId": "boundary", "ver": "1.0", "msgId": "...", "userInfo": { } },
  "BoundaryRelationships": [
    { "tenantId": "pg", "code": "VILLAGE_001", "hierarchyType": "ADMIN", "boundaryType": "Village", "parent": "LOCALITY_01" },
    { "tenantId": "pg", "code": "VILLAGE_002", "hierarchyType": "ADMIN", "boundaryType": "Village", "parent": "LOCALITY_01" }
  ]
}
```

| Field | Required | Notes |
| --- | --- | --- |
| `code` | yes | Boundary code; a matching boundary **entity** must already exist. Must not contain `\|`. |
| `tenantId` | yes | Tenant. Must not contain `\|`. |
| `hierarchyType` | yes | Must have a hierarchy definition. Must not contain `\|`. |
| `boundaryType` | yes | Must be part of the hierarchy definition. |
| `parent` | no | Parent boundary code; required for every non-root level and must already be persisted. Omit only for the root level. |

### Response — `200 OK`

```json
{
  "ResponseInfo": { "status": "successful" },
  "successfulBoundaryRelationships": [
    {
      "id": "a1b2...-uuid",
      "tenantId": "pg",
      "code": "VILLAGE_001",
      "hierarchyType": "ADMIN",
      "boundaryType": "Village",
      "parent": "LOCALITY_01",
      "auditDetails": { "createdBy": "...", "createdTime": 1718600000000, "lastModifiedBy": "...", "lastModifiedTime": 1718600000000 }
    }
  ],
  "failedBoundaryRelationships": [
    {
      "boundaryRelationship": { "tenantId": "pg", "code": "VILLAGE_002", "hierarchyType": "ADMIN", "boundaryType": "Village", "parent": "LOCALITY_99" },
      "errorCode": "PARENT_NOT_FOUND",
      "errorMessage": "Parent entity for current boundary relationship does not exist."
    }
  ]
}
```

A partial outcome still returns `200 OK`; the caller inspects the two lists. `successfulBoundaryRelationships` is the set accepted for (idempotent, asynchronous) persistence; `failedBoundaryRelationships` is the work to retry or escalate. A whole-request rejection (bad envelope) returns a structured `400`.

### Per-record error codes

| `errorCode` | Meaning | Caller action |
| --- | --- | --- |
| `BOUNDARY_ENTITY_DOES_NOT_EXIST` | No boundary entity exists for the given `code` + `tenantId`. | retry |
| `PARENT_NOT_FOUND` | The referenced `parent` relationship is not (yet) persisted. | retry |
| `BULK_RELATIONSHIP_PERSIST_TRANSIENT` | A transient DB error during validation reads, or a publish failure. | retry |
| `DUPLICATE_RECORD` | A relationship with this `code` already exists. | idempotent success |
| `DUPLICATE_RECORD_IN_REQUEST` | The same `(tenantId, hierarchyType, code)` appears more than once in this request. | fix input |
| `BOUNDARY_TYPE_ERROR` | `boundaryType` is not part of the hierarchy definition. | fix input |
| `HIERARCHY_ERROR` | The record's level is not a direct child of its parent's level (or a non-root record has no parent). | fix input |
| `HIERARCHY_DEFINITION_DOES_NOT_EXIST_ERR` | No hierarchy definition for the `tenantId` + `hierarchyType`. | fix input |
| `HIERARCHY_DEFINITION_INVALID_ERR` | The hierarchy definition is malformed (no root boundary type / no node with an empty parent). | investigate |
| `INVALID_BOUNDARY_CODE` | `code`/`tenantId`/`hierarchyType` contains the reserved `\|` delimiter. | fix input |
| `BULK_RELATIONSHIP_VALIDATION_ERROR` | An unexpected runtime error during per-record validation/enrichment (isolated so the rest of the batch proceeds). | investigate |

Whole-request `400` codes: `BULK_REQUEST_INFO_MISSING`, `BULK_REQUEST_EMPTY`, `BULK_REQUEST_SIZE_EXCEEDED`.

---

## Build & run

```bash
# Build
mvn clean install

# Run (after configuring datasource & kafka in application.properties)
mvn spring-boot:run
# or
java -jar target/boundary-service-1.0.1.jar
```

### Key configuration (`src/main/resources/application.properties`)

| Property | Default | Purpose |
| --- | --- | --- |
| `server.port` | `8081` | HTTP port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/postgres` | PostgreSQL connection (used for validation/search **reads** only) |
| `spring.flyway.enabled` | `false` | Enable to run DB migrations on startup |
| `spring.kafka.bootstrap-servers` | `localhost:9092` | Kafka brokers used by the producer. In DIGIT deployments the common Helm chart injects `SPRING_KAFKA_BOOTSTRAP_SERVERS`. |
| `spring.kafka.producer.properties.max.block.ms` | `15000` | Bounds how long a synchronous publish blocks if the broker is unreachable, so an outage fails fast (as a transient error the caller retries) instead of tying up request threads. |
| `kafka.topics.create.boundary.relationship` | `save-boundary-relationship` | Topic for **both** single and bulk relationship create, consumed by `egov-persister`. |
| `boundary.bulk.max.size` | `100` | Max records accepted by `/bulk/_create` (enforced in-service; keep ≥ the caller's chunk size). |
| `boundary.default.limit` / `boundary.max.default.limit` | `50` / `300` | Search paging defaults |

> Bulk creation writes through `egov-persister`, not directly to PostgreSQL, so the request path is not gated by the DB connection pool for writes. To make the persister aggregate high-volume creates into batched multi-row inserts, add `save-boundary-relationship` to the persister's `persister.batch.topics` (with `persister.bulk.enabled=true`); this is an **optional throughput optimization** — the topic is otherwise consumed one record at a time by the persister's normal listener, so bulk creation works on any persister deployment with no extra configuration.

---

## Database

Relationships are stored in `boundary_relationship` (`tenantId, code, hierarchyType` primary key; `ancestralMaterializedPath` holds the `|`-delimited ancestor chain used for subtree search). All writes go through the `egov-persister` mapping in `src/main/resources/boundary-persister.yml`, which uses `INSERT … ON CONFLICT (tenantid, code, hierarchytype) DO NOTHING` so at-least-once redelivery and caller re-submission are idempotent.

### Search indexes

Migration `V20260616120000__boundary_relationship_search_indexes.sql` adds two indexes that keep relationship/subtree search off full table scans (which, under concurrent campaign-scale search, were exhausting the connection pool):

- a **GIN** index on `string_to_array(ancestralmaterializedpath, '|')` — serves the `ARRAY[…] && …` subtree-overlap predicate;
- a `(tenantid, parent)` index — serves the `parent = ?` and root (`parent IS NULL`) branches.

They are plain (non-`CONCURRENTLY`) `CREATE INDEX IF NOT EXISTS` on purpose — `CONCURRENTLY` hangs inside a Flyway migration. For a very large existing table, build them out-of-band instead. See the migration header for details.
