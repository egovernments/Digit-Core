CREATE INDEX IF NOT EXISTS idx_mdms_data_query
ON eg_mdms_data (tenantid, schemacode, isactive, createdtime DESC);