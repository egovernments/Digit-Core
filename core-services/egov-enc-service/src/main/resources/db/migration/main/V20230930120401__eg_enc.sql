DROP TABLE IF EXISTS  eg_enc_mdms_config;

CREATE TABLE public."eg_enc_mdms_config"
(
    id SERIAL,
    tenant_id text NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX eg_enc_mdms_config_tenant_id ON eg_enc_mdms_config (tenant_id);
