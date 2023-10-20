
CREATE TABLE boundary (
  id VARCHAR PRIMARY KEY,
  tenantId VARCHAR,
  code VARCHAR,
  geometry JSONB,
  createdtime BIGINT NOT NULL,
  createdby VARCHAR(64) NOT NULL,
  lastmodifiedtime BIGINT,
  lastmodifiedby VARCHAR(64),

  CONSTRAINT check_id_length CHECK (length(id) <= 64),
  CONSTRAINT check_tenantId_length CHECK (length(tenantId) <= 64),
  CONSTRAINT check_code_length CHECK (length(code) <= 64),
  CONSTRAINT unique_code_tenantId UNIQUE (code, tenantId)
);