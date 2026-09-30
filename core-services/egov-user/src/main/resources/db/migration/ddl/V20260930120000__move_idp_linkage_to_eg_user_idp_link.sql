CREATE TABLE IF NOT EXISTS eg_user_idp_link (
    tenantid    varchar(256) NOT NULL,
    issuer      varchar(512) NOT NULL,
    subject     varchar(512) NOT NULL,
    userid      bigint       NOT NULL,
    uuid        varchar(300),
    providerid  varchar(64)  NOT NULL,
    createddate timestamp    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT eg_user_idp_link_pkey PRIMARY KEY (tenantid, providerid, issuer, subject),
    CONSTRAINT fk_eg_user_idp_link_user FOREIGN KEY (userid, tenantid) REFERENCES eg_user (id, tenantid)
);
CREATE INDEX IF NOT EXISTS idx_eg_user_idp_link_user ON eg_user_idp_link (userid, tenantid);

INSERT INTO eg_user_idp_link (tenantid, issuer, subject, userid, uuid, providerid)
SELECT tenantid, idpissuer, idpsubject, id, uuid, COALESCE(authprovider, 'UNKNOWN')
FROM eg_user WHERE idpsubject IS NOT NULL AND idpissuer IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE eg_user DROP CONSTRAINT IF EXISTS eg_user_idpissuer_idpsubject_tenantid_key;
ALTER TABLE eg_user DROP COLUMN IF EXISTS idpissuer, DROP COLUMN IF EXISTS idpsubject, DROP COLUMN IF EXISTS authprovider;
ALTER TABLE eg_user_audit_table DROP COLUMN IF EXISTS idpissuer, DROP COLUMN IF EXISTS idpsubject, DROP COLUMN IF EXISTS authprovider;
