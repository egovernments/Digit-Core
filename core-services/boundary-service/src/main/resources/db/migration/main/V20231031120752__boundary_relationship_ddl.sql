CREATE TABLE boundary_relationship (
    id VARCHAR(64),
    tenantId VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    hierarchyType VARCHAR(64) NOT NULL,
    boundaryType VARCHAR(64) NOT NULL,
    parent VARCHAR(64),
    ancestralMaterializedPath TEXT,
    createdtime BIGINT,
    createdby VARCHAR(64),
    lastmodifiedtime BIGINT,
    lastmodifiedby VARCHAR(64),
    CONSTRAINT uk_boundary_relationship UNIQUE (id),
    CONSTRAINT pk_boundary_relationship PRIMARY KEY (tenantId, code, hierarchyType)
);