CREATE TABLE IF NOT EXISTS public.eg_user_tenant_mapping (
    userid           bigint                 NOT NULL,
    type             character varying(50)  NOT NULL,
    tenantid         character varying(256) NOT NULL,
    usernamekey      character varying(512) NOT NULL,
    uuid             character varying(36),
    active           boolean                NOT NULL DEFAULT true,
    createddate      timestamp              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lastmodifieddate timestamp              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT eg_user_tenant_mapping_pkey PRIMARY KEY (userid, type, tenantid)
);
CREATE INDEX IF NOT EXISTS idx_eg_user_tenant_mapping_usernamekey_type
    ON public.eg_user_tenant_mapping (usernamekey, type) WHERE active;
