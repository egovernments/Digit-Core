CREATE TABLE uuid_vcid_mapper (
    uuid character varying(64),
    vcid character varying(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_uuid_vcid_mapper PRIMARY KEY (uuid)
);