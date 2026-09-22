-- One-time cleanup of legacy duplicate eg_user_session rows created before the
-- reclaim-on-login fix (every login used to insert a new row instead of reusing a terminal
-- one, so a user who logged in and out repeatedly accumulated one row per login episode).
-- Going forward, UserSessionService#createSession's reclaimTerminalSession keeps the table
-- from accumulating further duplicates, so this migration is a one-time catch-up, not an
-- ongoing job.
--
-- Nothing is deleted without first being preserved: every row removed here is copied into
-- eg_user_session_archive beforehand, so the full history remains queryable if ever needed —
-- this migration only moves rows out of the live "current session" table, it does not lose
-- data.

-- Archive table: same columns as eg_user_session, plus archivedtime for when this cleanup ran.
CREATE TABLE IF NOT EXISTS eg_user_session_archive (
    useruuid character varying(64) NOT NULL,
    tenantid character varying(256) NOT NULL,
    deviceid character varying(256),
    sessionid character varying(64) NOT NULL,
    status character varying(32) NOT NULL,
    createdtime bigint NOT NULL,
    lastservercontact bigint NOT NULL,
    version integer,
    archivedtime bigint NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_eg_user_session_archive_user_tenant
    ON eg_user_session_archive (useruuid, tenantid);

-- Step 1: archive every row that step 2 is about to remove. Ranking rule per (useruuid,
-- tenantid): keep the ACTIVE row if one exists, otherwise the most recently created row —
-- everything else (rn > 1) is a stale duplicate from a prior login episode and gets archived.
INSERT INTO eg_user_session_archive
    (useruuid, tenantid, deviceid, sessionid, status, createdtime, lastservercontact, version, archivedtime)
SELECT useruuid, tenantid, deviceid, sessionid, status, createdtime, lastservercontact, version,
       (extract(epoch FROM now()) * 1000)::bigint
FROM (
    SELECT *,
           ROW_NUMBER() OVER (
               PARTITION BY useruuid, tenantid
               ORDER BY (status = 'ACTIVE') DESC, createdtime DESC
           ) AS rn
    FROM eg_user_session
) ranked
WHERE rn > 1;

-- Step 2: remove the same rows from the live table — identical ranking, so exactly what was
-- archived in step 1 is what gets deleted here, never more.
DELETE FROM eg_user_session
WHERE ctid IN (
    SELECT ctid FROM (
        SELECT ctid,
               ROW_NUMBER() OVER (
                   PARTITION BY useruuid, tenantid
                   ORDER BY (status = 'ACTIVE') DESC, createdtime DESC
               ) AS rn
        FROM eg_user_session
    ) ranked
    WHERE rn > 1
);
