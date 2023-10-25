CREATE TABLE boundary_hierarchy (
    id VARCHAR(64),
    tenantId VARCHAR(64) NOT NULL,
    hierarchyType VARCHAR(64) NOT NULL,
    boundaryHierarchy JSONB NOT NULL,
    createdtime BIGINT,
    createdby VARCHAR(64),
    lastmodifiedtime BIGINT,
    lastmodifiedby VARCHAR(64),
    CONSTRAINT uk_boundary_hierarchy UNIQUE (id),
    CONSTRAINT pk_boundary_hierarchy PRIMARY KEY (tenantId, hierarchyType)
);