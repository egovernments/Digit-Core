CREATE TABLE eg_mdms_data (
    id VARCHAR(64) NOT NULL,
    tenantid VARCHAR(255) NOT NULL,
    uniqueidentifier VARCHAR(255),
    schemacode VARCHAR(255) NOT NULL,
    data JSONB NOT NULL,
    isactive BOOLEAN NOT NULL,
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,
    CONSTRAINT pk_eg_mdms_data PRIMARY KEY (tenantId,schemacode,uniqueidentifier),
    CONSTRAINT uk_eg_mdms_data UNIQUE(id)
);
