CREATE TABLE eg_login_audit (
    tenantid character varying(256) not null,
    userid character(36) not null,
    createdtime bigint not null,
    ipaddress character(36),
    loginstatus character(64),
    roles JSONB,
    logintype character(64)
);