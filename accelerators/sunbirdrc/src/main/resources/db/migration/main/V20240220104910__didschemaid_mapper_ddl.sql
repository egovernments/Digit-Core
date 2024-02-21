CREATE TABLE uuid_did_schemaid_mapper (
    uuid character varying(64),
    did character varying(64),
    schemaid character varying(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_uuid_did_schemaid_mapper PRIMARY KEY (uuid)
);