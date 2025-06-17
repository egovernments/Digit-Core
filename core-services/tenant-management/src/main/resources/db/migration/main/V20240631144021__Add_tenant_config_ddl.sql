CREATE TABLE tenant_config (
    id VARCHAR(128) PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    defaultLoginType VARCHAR(50),
    enableUserBasedLogin BOOLEAN,
    additionalAttributes JSONB, -- Storing additional attributes as JSONB
    isActive BOOLEAN NOT NULL,
    createdBy VARCHAR(64),
    lastModifiedBy VARCHAR(64),
    createdTime BIGINT,
    lastModifiedTime BIGINT
);
