CREATE TABLE IF NOT EXISTS eg_user_session (
    useruuid character varying(64) NOT NULL,
    tenantid character varying(256) NOT NULL,
    deviceid character varying(256),
    sessionid character varying(64) NOT NULL,
    status character varying(32) NOT NULL,
    createdtime bigint NOT NULL,
    lastservercontact bigint NOT NULL
);

-- Fast lookup for the hot authenticated-request path (validate by sessionId).
CREATE UNIQUE INDEX IF NOT EXISTS uk_eg_user_session_sessionid ON eg_user_session (sessionid);

-- Guarantees only one ACTIVE session per user+tenant even under concurrent logins:
-- the second concurrent INSERT fails on this constraint instead of racing a SELECT.
CREATE UNIQUE INDEX IF NOT EXISTS uk_eg_user_session_active_user_tenant
    ON eg_user_session (useruuid, tenantid)
    WHERE status = 'ACTIVE';
