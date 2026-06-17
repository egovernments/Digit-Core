-- ============================================================================
-- Local schema for the pgr.v3.yml persister config
-- (src/main/resources/pgr.v3.yml)
--
-- Target DB: the one in spring.datasource.url  ->  jdbc:postgresql://localhost:5432/pgr
-- Run with:
--     psql -h localhost -U postgres -d pgr -f pgr-v3-schema.sql
--
-- Tables are derived purely from the INSERT/UPDATE queries + jsonMaps in that
-- config. Column types are inferred from the JSON field semantics:
--   * epoch-millis timestamps (createdTime, when, expectedTime, ...) -> bigint
--   * lat / long / latitude / longitude                              -> double precision
--   * attributes / media (type: JSON, dbType: JSONB)                 -> jsonb
--   * isInternal                                                     -> boolean
--   * everything else                                                -> text / varchar
-- ============================================================================

-- ---------------------------------------------------------------------------
-- eg_pgr_service   (topic: save-pgr-service INSERT, update-pgr-service UPDATE)
-- UPDATE matches on (tenantid, servicerequestid)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eg_pgr_service (
    tenantid            character varying(256),
    servicecode         character varying(256),
    servicerequestid    character varying(256) PRIMARY KEY,
    description         text,
    lat                 double precision,
    "long"              double precision,
    addressid           character varying(256),
    address             text,
    email               character varying(256),
    deviceid            character varying(256),
    accountid           character varying(256),
    firstname           character varying(256),
    lastname            character varying(256),
    phone               character varying(64),
    attributes          jsonb,
    status              character varying(256),
    source              character varying(256),
    expectedtime        bigint,
    rating              integer,
    feedback            text,
    landmark            character varying(256),
    createdby           character varying(256),
    createdtime         bigint,
    lastmodifiedby      character varying(256),
    lastmodifiedtime    bigint
);

-- ---------------------------------------------------------------------------
-- eg_pgr_action   (topic: save-pgr-service & update-pgr-service INSERT)
-- "when" and "long"/"by" handling: "when" is a reserved word so it is quoted in
-- the config query; "by" is a non-reserved keyword and is used unquoted.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eg_pgr_action (
    uuid                character varying(256) PRIMARY KEY,
    by                  character varying(256),
    "when"              bigint,
    action              character varying(256),
    status              character varying(256),
    comments            text,
    media               jsonb,
    assignee            character varying(256),
    isinternal          boolean,
    tenantid            character varying(256),
    businesskey         character varying(256)
);

-- ---------------------------------------------------------------------------
-- eg_pgr_address   (topic: save-pgr-service INSERT)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eg_pgr_address (
    uuid                    character varying(256) PRIMARY KEY,
    housenoandstreetname    character varying(256),
    mohalla                 character varying(256),
    landmark                character varying(256),
    latitude                double precision,
    longitude               double precision,
    city                    character varying(256),
    tenantid                character varying(256),
    createdby               character varying(256),
    createdtime             bigint,
    lastmodifiedby          character varying(256),
    lastmodifiedtime        bigint
);
