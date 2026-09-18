-- Indexes supporting paginated search.
-- Both search queries filter by tenantid (and schemacode for master data) and order by
-- createdtime DESC with id as a tiebreaker. Matching the index to that shape lets Postgres
-- walk the index in order and stop after OFFSET + LIMIT rows instead of sorting the whole
-- filtered set on every page request.

CREATE INDEX IF NOT EXISTS idx_eg_mdms_data_tenant_schema_created
    ON eg_mdms_data (tenantid, schemacode, createdtime DESC, id);

CREATE INDEX IF NOT EXISTS idx_eg_mdms_schema_definition_tenant_created
    ON eg_mdms_schema_definition (tenantid, createdtime DESC, id);
