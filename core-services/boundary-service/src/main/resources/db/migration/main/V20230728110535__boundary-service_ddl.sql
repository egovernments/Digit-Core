
CREATE TABLE geometry (
  id VARCHAR PRIMARY KEY,
  type VARCHAR,
  coordinates JSONB,

  CONSTRAINT check_id_length CHECK (length(id) <= 64),
  CONSTRAINT check_type_length CHECK (length(type) <= 64)
);

CREATE TABLE boundary (
  id VARCHAR PRIMARY KEY,
  tenantId VARCHAR,
  code VARCHAR,
  geometryId VARCHAR,

  FOREIGN KEY (geometryId) REFERENCES geometry(id),
  CONSTRAINT check_id_length CHECK (length(id) <= 64),
  CONSTRAINT check_tenantId_length CHECK (length(tenantId) <= 64),
  CONSTRAINT check_code_length CHECK (length(code) <= 64),
  CONSTRAINT check_geometryId_length CHECK (length(geometryId) <= 64)
);