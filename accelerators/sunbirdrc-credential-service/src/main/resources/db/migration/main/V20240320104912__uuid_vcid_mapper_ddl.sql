CREATE TABLE uuid_vcid_mapper (
    uuid character varying(64),
    vcid character varying(64),
    createdBy character varying(64),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uuid_vcid_mapper_pkey PRIMARY KEY (uuid)
);