-- Indexes to make boundary-relationship subtree search index-accelerated instead of
-- sequentially scanning the whole boundary_relationship table on every search.
--
-- Why: BoundaryRelationshipService.getBoundaryRelationships() resolves a subtree
-- (includeChildren=true) with the array-overlap predicate built in BoundaryRelationshipQueryBuilder:
--     ... AND ARRAY[...]::text[] && string_to_array(ancestralmaterializedpath, '|')
-- That predicate is non-sargable, and the table had only the PK + unique(id), so every search did
-- a full Seq Scan, holding a DB connection for its duration. Under campaign-scale concurrent search
-- load this exhausts the Hikari pool (CannotGetJdbcConnectionException).
--
-- Validated on a 50k-row hierarchy (PostgreSQL 16): the GIN index turns the plan from a full
-- Seq Scan (~20 ms, scans all rows) into a Bitmap Index Scan (~2 ms). Measured with pgbench
-- (randomized subtree anchor, 1-48 concurrent clients on a 12-core box): ~10x higher search
-- throughput and ~10x lower latency under sustained concurrent load (at 48 clients, mean latency
-- ~498 ms -> ~51 ms; ~96 -> ~950 tps). The index removes only the scan cost; both plans still sort
-- and fetch the matched subtree rows, so the end-to-end gain is ~10x rather than the scan-node ratio.
--
-- 1) GIN index on the materialized-path token array -> serves the && overlap above.
-- 2) (tenantid, parent) -> serves the parent = ? and parent IS NULL (root) search branches.
--
-- NOTE: these are plain (non-CONCURRENTLY) CREATE INDEX statements on purpose. CREATE INDEX
-- CONCURRENTLY must NOT be used inside a Flyway migration here: even with
-- executeInTransaction=false, Flyway keeps its schema-history connection open in a transaction,
-- and CONCURRENTLY blocks waiting for that concurrent transaction to finish -> the migration hangs
-- indefinitely (verified against Flyway with this exact migration). A plain CREATE INDEX builds in
-- well under a second on this table under a brief, read-allowing ShareLock, which is acceptable for
-- this low-write table. For a very large table or a strict zero-write-downtime requirement, build
-- these indexes CONCURRENTLY out-of-band (manual DBA step) instead of via this migration.

CREATE INDEX IF NOT EXISTS idx_boundary_relationship_amp_gin
    ON boundary_relationship USING GIN (string_to_array(ancestralmaterializedpath, '|'));

CREATE INDEX IF NOT EXISTS idx_boundary_relationship_tenant_parent
    ON boundary_relationship (tenantid, parent);
