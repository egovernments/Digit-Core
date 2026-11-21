# Boundary Service

Spring Boot service that manages a tenant's administrative hierarchy — boundary **entities**, boundary **hierarchy definitions**, and the boundary **relationships** that record, for each boundary, its type, parent, and materialized path within a hierarchy (country → province → district → … → village).

- **Java:** 17
- **Framework:** Spring Boot 3.2.2
- **Datastore:** PostgreSQL (Flyway migrations under `src/main/resources/db/migration/main`)
- **Messaging:** Kafka. The single entity/relationship create & update are published to topics and written by `egov-persister`. In addition, boundary-service runs a **dedicated bulk-create consumer** that reads relationship batch-jobs from `boundary-relationship-bulk-create-job` and persists them synchronously (this is the horizontally-scalable path used for high-cardinality levels); per-record permanent failures and exhausted retries are dead-lettered to `boundary-relationship-bulk-create-error`.
- **Context path:** `/boundary-service` &nbsp;•&nbsp; **Default port:** `8081`

---

## API surface

All endpoints are `POST`. Base URL: `http://<host>:8081/boundary-service`.

| Resource | Path | Purpose | Write model |
| --- | --- | --- | --- |
| Hierarchy definition | `/boundary-hierarchy-definition/_create` | Define the boundary-type order for a hierarchy | async (Kafka) |
| Hierarchy definition | `/boundary-hierarchy-definition/_search` | Search hierarchy definitions | — |
| Boundary entity | `/boundary/_create` | Create boundary entities (geometry) | async (Kafka) |
| Boundary entity | `/boundary/_search` | Search boundary entities | — |
| Boundary entity | `/boundary/_update` | Update boundary entities | async (Kafka) |
| Boundary relationship | `/boundary-relationships/_create` | Create a **single** relationship | async (Kafka) → `202 Accepted` |
| Boundary relationship | **`/boundary-relationships/bulk/_create`** | **Create relationships in bulk** | **sync, committed** → `200 OK` |
| Boundary relationship | `/boundary-relationships/_search` | Search the relationship tree | — |
| Boundary relationship | `/boundary-relationships/_update` | Update a relationship's parent | async (Kafka) |

### Single vs. bulk relationship create

The single `_create` **validates then publishes to Kafka**; `egov-persister` writes the row asynchronously. The API returns `202 Accepted` — acceptance, not a committed write. This is fine for one record but, at scale, makes it hard to (a) know a record is actually persisted before creating its children, and (b) detect a write that was accepted but silently failed to persist.

The bulk endpoint exists to remove both problems for high-volume hierarchy creation. See [`docs/Bulk-Boundary-Relationship-Creation-Design.docx`](docs/Bulk-Boundary-Relationship-Creation-Design.docx) for the design rationale and [`docs/Bulk-Boundary-Relationship-Flow.docx`](docs/Bulk-Boundary-Relationship-Flow.docx) for the end-to-end flow, failure handling, and validation results.

---

## Bulk Boundary Relationship Creation

`POST /boundary-relationships/bulk/_create`

Creates many relationships in one request, **synchronously**, with a **per-record outcome**. The valid records are committed inside a single database transaction (a deterministic, durable write — not a Kafka publish), and the response reports exactly which records were created and which failed, with a reason for each. Individual record failures do **not** fail the whole request.

### Semantics

- **Per-record validation.** Each record runs the same business rules as the single create (boundary entity exists, no duplicate, parent exists, correct hierarchy level). A record that fails validation is reported in `failedBoundaryRelationships`; the rest continue.
- **Synchronous, atomic persistence.** All validated records are inserted in one `@Transactional` batch. On success they are durably committed and returned in `successfulBoundaryRelationships` (with their generated `id`). If the commit itself fails (a database error), nothing is persisted and the whole validated set is reported as failed with `BULK_RELATIONSHIP_PERSIST_FAILED` — clean for resubmission.
- **Intra-batch de-duplication.** Two records with the same `(tenantId, hierarchyType, code)` in one request are rejected (`DUPLICATE_RECORD_IN_REQUEST`) so a single duplicate cannot trip the primary key and fail the batch.
- **Batch size.** 1–100 records per request (`@Size(min = 1, max = 100)`). Larger levels must be chunked by the caller.

### Caller contract

The endpoint expects each batch to be a set of **siblings whose parent is already persisted**:

1. Create the hierarchy **top-down**, one level at a time (country, then province, …). Confirm a level is persisted before creating its children — the bulk write being synchronous and committed makes this a genuine barrier.
2. For high-cardinality lower levels (locality, village), split into batches of ≤ 100 where **every record in a batch shares one already-persisted parent** (e.g. villages grouped by their parent locality).
3. On failures, act per record. Because re-submission targets only the missing records, the operation is idempotent in practice. (Note: re-submitting a relationship that already exists currently returns `DUPLICATE_RECORD` for that record, not a silent no-op.)

> The **caller** (e.g. boundary-management) owns reading the sheet, generating codes, chunking, and level sequencing. **Job distribution across workers is handled by this service's Kafka consumer** (below) — the caller simply produces batch-jobs to the topic; it does not need its own worker pool. The same per-record write logic backs both the HTTP endpoint and the Kafka consumer.

---

## Kafka bulk-create consumer (horizontal scaling)

`@KafkaListener` on **`boundary-relationship-bulk-create-job`** (`BoundaryRelationshipBulkConsumer`).

For high-cardinality lower levels, the caller publishes each chunk of ≤ 100 sibling records as one job, **keyed by the parent code**. The consumer runs the **same** `createBulkBoundaryRelationship` logic as the HTTP endpoint. Running several boundary-service replicas in the one consumer group (`spring.kafka.consumer.group-id=boundary-service`) spreads the topic's partitions across replicas, so creation scales horizontally; `spring.kafka.listener.concurrency` adds per-replica threads (effective parallelism = `min(concurrency, partitions)`).

- **Keying by parent** keeps a parent's children together and ordered on one partition.
- **Pre-create the topic with partitions ≥ the number of consumer replicas** — broker auto-create gives a single partition, which collapses all work onto one consumer.
- Jobs are structurally validated on the Kafka path (the controller's `@Valid` does not run here); a structurally invalid or unparseable job is treated as permanent and sent to the error topic.

### Failure handling (per job)

Every record ends in exactly one outcome — nothing is silently dropped:

| Outcome | Trigger | What the consumer does |
| --- | --- | --- |
| **Created** | valid + persisted | committed; counted as success |
| **Already exists** | `DUPLICATE_RECORD` on a re-run | treated as idempotent success (safe under at-least-once) |
| **Transient** | `PARENT_NOT_FOUND`, `BOUNDARY_ENTITY_DOES_NOT_EXIST`, or `BULK_RELATIONSHIP_PERSIST_TRANSIENT` (deadlock / serialization / connection blip) | **throws → the whole job is redelivered** with non-blocking pause-backoff (`2 s × 150 ≈ 5 min`) until it succeeds; if still failing after the ceiling it is **dead-lettered** to the error topic. Already-created records are idempotent no-ops on redelivery. |
| **Permanent** | bad data (`HIERARCHY_ERROR`, `BOUNDARY_TYPE_ERROR`, …) or `BULK_RELATIONSHIP_PERSIST_FAILED` | published **once** to the error topic; the offset commits (no pointless retry). **Other valid records in the same job are still created.** |

The backoff is **non-blocking** (`ContainerPausingBackOffHandler` pauses the partition and schedules a resume), so the consumer keeps polling and `max.poll.interval.ms` is never breached regardless of the ceiling. Because keying is by parent, **a job that is retrying holds up later jobs on the same partition** until it succeeds or is dead-lettered (per-partition head-of-line). Idempotency comes from `INSERT … ON CONFLICT (tenantid, code, hierarchytype) DO NOTHING`, so a parent that is briefly late simply causes its children's jobs to retry until it lands — ordering across levels is *not* required.

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
| `code` | yes | Boundary code; a matching boundary **entity** must already exist. |
| `tenantId` | yes | Tenant. |
| `hierarchyType` | yes | Must have a hierarchy definition. |
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

A partial outcome still returns `200 OK`; the caller inspects the two lists. Callers should treat this contract explicitly — `successfulBoundaryRelationships` is the durably-persisted set, `failedBoundaryRelationships` is the work to retry or escalate.

### Per-record error codes

| `errorCode` | Meaning |
| --- | --- |
| `BOUNDARY_ENTITY_DOES_NOT_EXIST` | No boundary entity exists for the given `code` + `tenantId`. |
| `PARENT_NOT_FOUND` | The referenced `parent` relationship is not (yet) persisted. |
| `DUPLICATE_RECORD` | A relationship with this `code` already exists in the database. |
| `DUPLICATE_RECORD_IN_REQUEST` | The same `(tenantId, hierarchyType, code)` appears more than once in this request. |
| `BOUNDARY_TYPE_ERROR` | `boundaryType` is not part of the hierarchy definition. |
| `HIERARCHY_ERROR` | The record's level is not a direct child of its parent's level (or a non-root record has no parent). |
| `HIERARCHY_DEFINITION_DOES_NOT_EXIST_ERR` | No hierarchy definition for the `tenantId` + `hierarchyType`. |
| `BULK_RELATIONSHIP_VALIDATION_ERROR` | A non-business runtime error during per-record validation/enrichment (isolated so the rest of the batch proceeds). |
| `BULK_RELATIONSHIP_PERSIST_TRANSIENT` | The atomic commit hit a transient DB error (deadlock / serialization / connection); nothing persisted — **retryable** (the Kafka consumer redelivers). |
| `BULK_RELATIONSHIP_PERSIST_FAILED` | The atomic commit failed with a non-transient database error; no record in the validated batch was persisted. |

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
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/postgres` | PostgreSQL connection (used by the synchronous bulk write) |
| `spring.flyway.enabled` | `false` | Enable to run DB migrations on startup |
| `spring.kafka.bootstrap-servers` | `localhost:9092` | Kafka brokers used by both the producer and the bulk consumer. In DIGIT deployments the common Helm chart injects `SPRING_KAFKA_BOOTSTRAP_SERVERS` for every `java-spring` service. |
| `kafka.topics.create.boundary.relationship` | `save-boundary-relationship` | Topic for the async single create (consumed by `egov-persister`) |
| `kafka.topics.bulk.create.boundary.relationship.job` | `boundary-relationship-bulk-create-job` | Batch-job topic consumed by the bulk consumer (key = parent code) |
| `kafka.topics.bulk.create.boundary.relationship.error` | `boundary-relationship-bulk-create-error` | Dead-letter / per-record permanent-failure topic |
| `spring.kafka.consumer.group-id` | `boundary-service` | Consumer group — run multiple replicas in it to scale the bulk path |
| `spring.kafka.listener.concurrency` | `3` | Consumer threads per replica (effective parallelism = `min(concurrency, partitions)`) |
| `boundary.default.limit` / `boundary.max.default.limit` | `50` / `300` | Search paging defaults |

> The bulk endpoint writes directly to PostgreSQL via JDBC, so it depends on the datasource and its connection pool. When parallelising bulk submission from the caller, front the database with a connection pool (PgBouncer / PgPool / RDS Proxy) so concurrent requests queue for a connection instead of failing.

---

## Database

Relationships are stored in `boundary_relationship` (`tenantId, code, hierarchyType` primary key; `ancestralMaterializedPath` holds the `|`-delimited ancestor chain used for subtree search). The bulk insert mirrors the columns written by the `egov-persister` mapping in `src/main/resources/boundary-persister.yml`, and uses `ON CONFLICT (tenantid, code, hierarchytype) DO NOTHING` so at-least-once redelivery from the consumer is idempotent.

### Search indexes

Migration `V20260616120000__boundary_relationship_search_indexes.sql` adds two indexes that keep relationship/subtree search off full table scans (which, under concurrent campaign-scale search, were exhausting the connection pool):

- a **GIN** index on `string_to_array(ancestralmaterializedpath, '|')` — serves the `ARRAY[…] && …` subtree-overlap predicate;
- a `(tenantid, parent)` index — serves the `parent = ?` and root (`parent IS NULL`) branches.

They are plain (non-`CONCURRENTLY`) `CREATE INDEX IF NOT EXISTS` on purpose — `CONCURRENTLY` hangs inside a Flyway migration. For a very large existing table, build them out-of-band instead. See the migration header for details.
