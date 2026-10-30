-- Maps a boundary (tenantId + hierarchyType + code) to an identifierId.
-- The mapping is one-to-one within a tenant and hierarchy: a boundary code has at most one identifierId
-- and an identifierId belongs to at most one boundary code. Both unique constraints also serve as the
-- indexes for search by tenantId + hierarchyType (optionally with codes or identifierIds).
CREATE TABLE IF NOT EXISTS boundary_identifier (
  id VARCHAR (64) NOT NULL,
  tenantId VARCHAR (64) NOT NULL,
  hierarchyType VARCHAR (64) NOT NULL,
  code VARCHAR (64) NOT NULL,
  identifierId VARCHAR (128) NOT NULL,
  createdtime BIGINT NOT NULL,
  createdby VARCHAR (64) NOT NULL,
  lastmodifiedtime BIGINT,
  lastmodifiedby VARCHAR (64),

  CONSTRAINT pk_boundary_identifier PRIMARY KEY (id),
  CONSTRAINT uk_boundary_identifier_tenantid_hierarchytype_code UNIQUE (tenantId, hierarchyType, code),
  CONSTRAINT uk_boundary_identifier_tenantid_hierarchytype_identifierid UNIQUE (tenantId, hierarchyType, identifierId)
);
