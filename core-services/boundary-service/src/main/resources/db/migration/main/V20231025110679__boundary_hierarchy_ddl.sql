CREATE TABLE boundary_hierarchy (
    id VARCHAR(64),
    tenantId VARCHAR(64) NOT NULL,
    hierarchyType VARCHAR(64) NOT NULL,
    boundaryHierarchy JSONB NOT NULL,
    createdtime BIGINT,
    createdby VARCHAR(64),
    lastmodifiedtime BIGINT,
    lastmodifiedby VARCHAR(64),
    CONSTRAINT pk_boundary_hierarchy PRIMARY KEY (id),
    CONSTRAINT uk_boundary_hierarchy UNIQUE (tenantId , hierarchyType)
);
-- Create an index on tenantId and hierarchyType
CREATE INDEX idx_boundary_hierarchy_tenantId_hierarchyType ON boundary_hierarchy (tenantId , hierarchyType);
