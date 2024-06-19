
CREATE TABLE boundary (
  id VARCHAR (64),
  tenantId VARCHAR (64) NOT NULL,
  code VARCHAR (64) NOT NULL,
  geometry JSONB,
  additionalDetails JSONB,
  createdtime BIGINT NOT NULL,
  createdby VARCHAR (64) NOT NULL,
  lastmodifiedtime BIGINT,
  lastmodifiedby VARCHAR (64),

  CONSTRAINT unique_code_tenantId UNIQUE (code, tenantId),
  PRIMARY KEY (id)
);
-- Create an index on tenantId and code
CREATE INDEX idx_boundary_tenantId_code ON boundary (tenantId, code);
