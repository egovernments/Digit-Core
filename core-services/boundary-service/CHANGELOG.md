# Changelog

All notable changes to this module will be documented in this file.

## 1.0.2 - 2026-07-20
- Bulk relationship create API (`POST /boundary-relationships/bulk/_create`): relationships are validated + enriched synchronously and published together as **one array message keyed by the shared parent code** to a dedicated bulk topic, instead of one message per record — new models `BulkBoundaryRelationshipRequest` / `BulkBoundaryRelationshipRequestDTO` / `BulkBoundaryRelationshipResponse` / `FailedBoundaryRelationship`
- Persister config (`boundary-persister.yml`) now carries **two separate relationship queryMaps** — a single-object map for `save-boundary-relationship` (single create) and an array map for the dedicated bulk topic
- Boundary relationship creation performance enhancement: query-builder and repository bulk paths reworked; new search-index migration `V20260616120000__boundary_relationship_search_indexes.sql`
- `correlationId` + `tenantId` now propagated across Kafka on publish (via tracer `2.9.3-SNAPSHOT`)
- New error codes and configurable properties added (`ApplicationProperties`, `ErrorCodes`, `application.properties`)
- Code-review (CodeRabbit) fixes applied
