CREATE TABLE eg_login_audit (
    tenantid character varying(256) not null,
    userid character varying(36) not null,
    createdtime bigint not null,
    ipaddress character varying(36),
    loginstatus character varying(64),
    roles JSONB,
    logintype character varying(64)
);