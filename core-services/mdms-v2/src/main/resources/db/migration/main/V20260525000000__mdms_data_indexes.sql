CREATE INDEX IF NOT EXISTS idx_eg_mdms_data_gin_data ON eg_mdms_data USING GIN (data jsonb_path_ops);
CREATE INDEX IF NOT EXISTS idx_eg_mdms_data_createdtime ON eg_mdms_data (createdtime DESC);
CREATE INDEX IF NOT EXISTS idx_eg_mdms_data_tenantid ON eg_mdms_data (tenantid);
-- text_pattern_ops allows btree index to serve LIKE 'prefix%' queries on default collation (V1 search path)
CREATE INDEX IF NOT EXISTS idx_eg_mdms_data_tenantid_pattern ON eg_mdms_data (tenantid text_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_eg_mdms_data_schemacode ON eg_mdms_data (schemacode);
CREATE INDEX IF NOT EXISTS idx_eg_mdms_data_uniqueidentifier ON eg_mdms_data (uniqueidentifier);
CREATE INDEX IF NOT EXISTS idx_eg_mdms_data_tenant_schema ON eg_mdms_data (tenantid, schemacode);
CREATE INDEX IF NOT EXISTS idx_eg_mdms_schema_def_code ON eg_mdms_schema_definition (code);
CREATE INDEX IF NOT EXISTS idx_eg_mdms_schema_def_tenant_code ON eg_mdms_schema_definition (tenantid, code);