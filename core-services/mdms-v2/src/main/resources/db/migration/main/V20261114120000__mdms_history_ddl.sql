-- MDMS version history: row triggers copy every change to eg_mdms_schema_definition and
-- eg_mdms_data into append-only *_history tables in the same transaction, whoever the writer is.
-- operation: MIGRATION, INSERT, UPDATE, DELETE or TRUNCATE. A key change is recorded as DELETE + INSERT.
-- IMPORTANT: dropping or renaming a source column without updating the history table and trigger
-- functions in the same migration makes every write to that table fail.

-- Fail fast instead of queueing writers; on timeout the migration rolls back and can be re-run.
SET LOCAL lock_timeout = '10s';

-- Block writers (not readers) so no change falls between the baseline copy and the triggers.
LOCK TABLE eg_mdms_schema_definition, eg_mdms_data IN SHARE ROW EXCLUSIVE MODE;

-- String columns are unbounded so widening a source column can never make the trigger fail.
CREATE TABLE eg_mdms_schema_definition_history (
    historyid BIGSERIAL NOT NULL,
    version INTEGER NOT NULL,
    operation VARCHAR NOT NULL,
    id VARCHAR,
    tenantid VARCHAR NOT NULL,
    code VARCHAR NOT NULL,
    description VARCHAR,
    definition JSONB,
    isactive BOOLEAN,
    createdBy VARCHAR,
    lastModifiedBy VARCHAR,
    createdTime BIGINT,
    lastModifiedTime BIGINT,
    changedTime BIGINT NOT NULL,
    dbUser VARCHAR,
    clientAddress VARCHAR,
    applicationName VARCHAR,
    transactionId BIGINT,
    CONSTRAINT pk_eg_mdms_schema_definition_history PRIMARY KEY (historyid),
    CONSTRAINT uk_eg_mdms_schema_definition_history_version UNIQUE (tenantid, code, version),
    CONSTRAINT chk_eg_mdms_schema_definition_history_operation CHECK (operation IN ('MIGRATION', 'INSERT', 'UPDATE', 'DELETE', 'TRUNCATE'))
);

CREATE INDEX idx_eg_mdms_schema_definition_history_changedtime ON eg_mdms_schema_definition_history (tenantid, changedTime);

CREATE TABLE eg_mdms_data_history (
    historyid BIGSERIAL NOT NULL,
    version INTEGER NOT NULL,
    operation VARCHAR NOT NULL,
    id VARCHAR,
    tenantid VARCHAR NOT NULL,
    uniqueidentifier VARCHAR NOT NULL,
    schemacode VARCHAR NOT NULL,
    data JSONB,
    isactive BOOLEAN,
    createdBy VARCHAR,
    lastModifiedBy VARCHAR,
    createdTime BIGINT,
    lastModifiedTime BIGINT,
    changedTime BIGINT NOT NULL,
    dbUser VARCHAR,
    clientAddress VARCHAR,
    applicationName VARCHAR,
    transactionId BIGINT,
    CONSTRAINT pk_eg_mdms_data_history PRIMARY KEY (historyid),
    CONSTRAINT uk_eg_mdms_data_history_version UNIQUE (tenantid, schemacode, uniqueidentifier, version),
    CONSTRAINT chk_eg_mdms_data_history_operation CHECK (operation IN ('MIGRATION', 'INSERT', 'UPDATE', 'DELETE', 'TRUNCATE'))
);

CREATE INDEX idx_eg_mdms_data_history_changedtime ON eg_mdms_data_history (tenantid, changedTime);
CREATE INDEX idx_eg_mdms_data_history_id ON eg_mdms_data_history (id);

-- Existing rows are recorded as version 1 with operation MIGRATION.
INSERT INTO eg_mdms_schema_definition_history (version, operation, id, tenantid, code, description, definition, isactive,
        createdBy, lastModifiedBy, createdTime, lastModifiedTime, changedTime, dbUser, clientAddress, applicationName, transactionId)
SELECT 1, 'MIGRATION', s.id, s.tenantid, s.code, s.description, s.definition, s.isactive,
        s.createdBy, s.lastModifiedBy, s.createdTime, s.lastModifiedTime,
        (EXTRACT(EPOCH FROM pg_catalog.clock_timestamp())::NUMERIC OPERATOR(pg_catalog.*) 1000)::BIGINT, session_user, pg_catalog.inet_client_addr()::VARCHAR,
        NULLIF(pg_catalog.current_setting('application_name', true), ''), pg_catalog.txid_current()
FROM eg_mdms_schema_definition s;

INSERT INTO eg_mdms_data_history (version, operation, id, tenantid, uniqueidentifier, schemacode, data, isactive,
        createdBy, lastModifiedBy, createdTime, lastModifiedTime, changedTime, dbUser, clientAddress, applicationName, transactionId)
SELECT 1, 'MIGRATION', d.id, d.tenantid, d.uniqueidentifier, d.schemacode, d.data, d.isactive,
        d.createdBy, d.lastModifiedBy, d.createdTime, d.lastModifiedTime,
        (EXTRACT(EPOCH FROM pg_catalog.clock_timestamp())::NUMERIC OPERATOR(pg_catalog.*) 1000)::BIGINT, session_user, pg_catalog.inet_client_addr()::VARCHAR,
        NULLIF(pg_catalog.current_setting('application_name', true), ''), pg_catalog.txid_current()
FROM eg_mdms_data d;

-- SECURITY DEFINER: writers need no grant on the history tables; search_path is pinned below.
-- Every function and operator is schema-qualified (pg_catalog) so an object planted on the
-- search_path cannot run with the owner's privileges. Keep this rule when editing.
CREATE OR REPLACE FUNCTION fn_eg_mdms_schema_definition_history() RETURNS TRIGGER AS $$
DECLARE
    v_rows eg_mdms_schema_definition[];
    v_operations TEXT[];
    i INTEGER;
BEGIN
    IF TG_OP = 'INSERT' THEN
        v_rows := ARRAY[NEW];
        v_operations := ARRAY['INSERT'];
    ELSIF TG_OP = 'DELETE' THEN
        v_rows := ARRAY[OLD];
        v_operations := ARRAY['DELETE'];
    ELSE
        -- Skip updates that do not change anything
        IF OLD OPERATOR(pg_catalog.=) NEW THEN
            RETURN NULL;
        END IF;
        IF OLD.tenantid::TEXT IS DISTINCT FROM NEW.tenantid::TEXT OR OLD.code::TEXT IS DISTINCT FROM NEW.code::TEXT THEN
            v_rows := ARRAY[OLD, NEW];
            v_operations := ARRAY['DELETE', 'INSERT'];
        ELSE
            v_rows := ARRAY[NEW];
            v_operations := ARRAY['UPDATE'];
        END IF;
    END IF;

    FOR i IN 1 .. pg_catalog.array_length(v_rows, 1) LOOP
        INSERT INTO eg_mdms_schema_definition_history (version, operation, id, tenantid, code, description, definition, isactive,
                createdBy, lastModifiedBy, createdTime, lastModifiedTime, changedTime, dbUser, clientAddress, applicationName, transactionId)
        SELECT COALESCE(pg_catalog.max(h.version), 0) OPERATOR(pg_catalog.+) 1, v_operations[i], (v_rows[i]).id, (v_rows[i]).tenantid, (v_rows[i]).code,
                (v_rows[i]).description, (v_rows[i]).definition, (v_rows[i]).isactive,
                (v_rows[i]).createdBy, (v_rows[i]).lastModifiedBy, (v_rows[i]).createdTime, (v_rows[i]).lastModifiedTime,
                (EXTRACT(EPOCH FROM pg_catalog.clock_timestamp())::NUMERIC OPERATOR(pg_catalog.*) 1000)::BIGINT, session_user, pg_catalog.inet_client_addr()::VARCHAR,
                NULLIF(pg_catalog.current_setting('application_name', true), ''), pg_catalog.txid_current()
        FROM eg_mdms_schema_definition_history h
        WHERE h.tenantid OPERATOR(pg_catalog.=) (v_rows[i]).tenantid AND h.code OPERATOR(pg_catalog.=) (v_rows[i]).code
        ON CONFLICT ON CONSTRAINT uk_eg_mdms_schema_definition_history_version DO NOTHING;

        -- Not expected (same-key writers are serialised by the primary key); report it as retryable.
        IF NOT FOUND THEN
            RAISE EXCEPTION 'concurrent change to MDMS schema % (tenant %), retry the transaction', (v_rows[i]).code, (v_rows[i]).tenantid
                USING ERRCODE = 'serialization_failure';
        END IF;
    END LOOP;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION fn_eg_mdms_data_history() RETURNS TRIGGER AS $$
DECLARE
    v_rows eg_mdms_data[];
    v_operations TEXT[];
    i INTEGER;
BEGIN
    IF TG_OP = 'INSERT' THEN
        v_rows := ARRAY[NEW];
        v_operations := ARRAY['INSERT'];
    ELSIF TG_OP = 'DELETE' THEN
        v_rows := ARRAY[OLD];
        v_operations := ARRAY['DELETE'];
    ELSE
        -- Skip updates that do not change anything
        IF OLD OPERATOR(pg_catalog.=) NEW THEN
            RETURN NULL;
        END IF;
        IF OLD.tenantid::TEXT IS DISTINCT FROM NEW.tenantid::TEXT OR OLD.schemacode::TEXT IS DISTINCT FROM NEW.schemacode::TEXT
           OR OLD.uniqueidentifier::TEXT IS DISTINCT FROM NEW.uniqueidentifier::TEXT THEN
            v_rows := ARRAY[OLD, NEW];
            v_operations := ARRAY['DELETE', 'INSERT'];
        ELSE
            v_rows := ARRAY[NEW];
            v_operations := ARRAY['UPDATE'];
        END IF;
    END IF;

    FOR i IN 1 .. pg_catalog.array_length(v_rows, 1) LOOP
        INSERT INTO eg_mdms_data_history (version, operation, id, tenantid, uniqueidentifier, schemacode, data, isactive,
                createdBy, lastModifiedBy, createdTime, lastModifiedTime, changedTime, dbUser, clientAddress, applicationName, transactionId)
        SELECT COALESCE(pg_catalog.max(h.version), 0) OPERATOR(pg_catalog.+) 1, v_operations[i], (v_rows[i]).id, (v_rows[i]).tenantid, (v_rows[i]).uniqueidentifier,
                (v_rows[i]).schemacode, (v_rows[i]).data, (v_rows[i]).isactive,
                (v_rows[i]).createdBy, (v_rows[i]).lastModifiedBy, (v_rows[i]).createdTime, (v_rows[i]).lastModifiedTime,
                (EXTRACT(EPOCH FROM pg_catalog.clock_timestamp())::NUMERIC OPERATOR(pg_catalog.*) 1000)::BIGINT, session_user, pg_catalog.inet_client_addr()::VARCHAR,
                NULLIF(pg_catalog.current_setting('application_name', true), ''), pg_catalog.txid_current()
        FROM eg_mdms_data_history h
        WHERE h.tenantid OPERATOR(pg_catalog.=) (v_rows[i]).tenantid AND h.schemacode OPERATOR(pg_catalog.=) (v_rows[i]).schemacode
          AND h.uniqueidentifier OPERATOR(pg_catalog.=) (v_rows[i]).uniqueidentifier
        ON CONFLICT ON CONSTRAINT uk_eg_mdms_data_history_version DO NOTHING;

        -- Not expected (same-key writers are serialised by the primary key); report it as retryable.
        IF NOT FOUND THEN
            RAISE EXCEPTION 'concurrent change to MDMS data % / % (tenant %), retry the transaction', (v_rows[i]).schemacode,
                (v_rows[i]).uniqueidentifier, (v_rows[i]).tenantid
                USING ERRCODE = 'serialization_failure';
        END IF;
    END LOOP;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- TRUNCATE does not fire row triggers, so copy every row before it is removed (operation TRUNCATE).
CREATE OR REPLACE FUNCTION fn_eg_mdms_schema_definition_history_truncate() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO eg_mdms_schema_definition_history (version, operation, id, tenantid, code, description, definition, isactive,
            createdBy, lastModifiedBy, createdTime, lastModifiedTime, changedTime, dbUser, clientAddress, applicationName, transactionId)
    SELECT COALESCE(v.maxversion, 0) OPERATOR(pg_catalog.+) 1, 'TRUNCATE', s.id, s.tenantid, s.code, s.description, s.definition, s.isactive,
            s.createdBy, s.lastModifiedBy, s.createdTime, s.lastModifiedTime,
            (EXTRACT(EPOCH FROM pg_catalog.clock_timestamp())::NUMERIC OPERATOR(pg_catalog.*) 1000)::BIGINT, session_user, pg_catalog.inet_client_addr()::VARCHAR,
            NULLIF(pg_catalog.current_setting('application_name', true), ''), pg_catalog.txid_current()
    FROM eg_mdms_schema_definition s
    LEFT JOIN (SELECT h.tenantid, h.code, pg_catalog.max(h.version) AS maxversion
               FROM eg_mdms_schema_definition_history h GROUP BY h.tenantid, h.code) v
           ON v.tenantid OPERATOR(pg_catalog.=) s.tenantid AND v.code OPERATOR(pg_catalog.=) s.code;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION fn_eg_mdms_data_history_truncate() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO eg_mdms_data_history (version, operation, id, tenantid, uniqueidentifier, schemacode, data, isactive,
            createdBy, lastModifiedBy, createdTime, lastModifiedTime, changedTime, dbUser, clientAddress, applicationName, transactionId)
    SELECT COALESCE(v.maxversion, 0) OPERATOR(pg_catalog.+) 1, 'TRUNCATE', d.id, d.tenantid, d.uniqueidentifier, d.schemacode, d.data, d.isactive,
            d.createdBy, d.lastModifiedBy, d.createdTime, d.lastModifiedTime,
            (EXTRACT(EPOCH FROM pg_catalog.clock_timestamp())::NUMERIC OPERATOR(pg_catalog.*) 1000)::BIGINT, session_user, pg_catalog.inet_client_addr()::VARCHAR,
            NULLIF(pg_catalog.current_setting('application_name', true), ''), pg_catalog.txid_current()
    FROM eg_mdms_data d
    LEFT JOIN (SELECT h.tenantid, h.schemacode, h.uniqueidentifier, pg_catalog.max(h.version) AS maxversion
               FROM eg_mdms_data_history h GROUP BY h.tenantid, h.schemacode, h.uniqueidentifier) v
           ON v.tenantid OPERATOR(pg_catalog.=) d.tenantid AND v.schemacode OPERATOR(pg_catalog.=) d.schemacode
          AND v.uniqueidentifier OPERATOR(pg_catalog.=) d.uniqueidentifier;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- History is append-only. A retention purge must be done by the owner after disabling trg_*_history_immutable.
CREATE OR REPLACE FUNCTION fn_eg_mdms_history_immutable() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'MDMS history table % is append-only, % is not allowed', TG_TABLE_NAME, TG_OP
        USING ERRCODE = 'insufficient_privilege';
END;
$$ LANGUAGE plpgsql;

-- Only the history triggers (trigger depth >= 2) may insert history rows.
CREATE OR REPLACE FUNCTION fn_eg_mdms_history_insert_guard() RETURNS TRIGGER AS $$
BEGIN
    IF pg_catalog.pg_trigger_depth() OPERATOR(pg_catalog.<) 2 THEN
        RAISE EXCEPTION 'MDMS history table % can only be written by the history triggers', TG_TABLE_NAME
            USING ERRCODE = 'insufficient_privilege';
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- pg_catalog first, then the schema this migration runs in, pg_temp last.
DO $pin$
BEGIN
    EXECUTE pg_catalog.format('ALTER FUNCTION fn_eg_mdms_schema_definition_history() SET search_path = pg_catalog, %I, pg_temp', pg_catalog.current_schema());
    EXECUTE pg_catalog.format('ALTER FUNCTION fn_eg_mdms_data_history() SET search_path = pg_catalog, %I, pg_temp', pg_catalog.current_schema());
    EXECUTE pg_catalog.format('ALTER FUNCTION fn_eg_mdms_schema_definition_history_truncate() SET search_path = pg_catalog, %I, pg_temp', pg_catalog.current_schema());
    EXECUTE pg_catalog.format('ALTER FUNCTION fn_eg_mdms_data_history_truncate() SET search_path = pg_catalog, %I, pg_temp', pg_catalog.current_schema());
END
$pin$;

CREATE TRIGGER trg_eg_mdms_schema_definition_history
    AFTER INSERT OR UPDATE OR DELETE ON eg_mdms_schema_definition
    FOR EACH ROW EXECUTE PROCEDURE fn_eg_mdms_schema_definition_history();

CREATE TRIGGER trg_eg_mdms_schema_definition_history_truncate
    BEFORE TRUNCATE ON eg_mdms_schema_definition
    FOR EACH STATEMENT EXECUTE PROCEDURE fn_eg_mdms_schema_definition_history_truncate();

CREATE TRIGGER trg_eg_mdms_data_history
    AFTER INSERT OR UPDATE OR DELETE ON eg_mdms_data
    FOR EACH ROW EXECUTE PROCEDURE fn_eg_mdms_data_history();

CREATE TRIGGER trg_eg_mdms_data_history_truncate
    BEFORE TRUNCATE ON eg_mdms_data
    FOR EACH STATEMENT EXECUTE PROCEDURE fn_eg_mdms_data_history_truncate();

CREATE TRIGGER trg_eg_mdms_schema_definition_history_immutable
    BEFORE UPDATE OR DELETE OR TRUNCATE ON eg_mdms_schema_definition_history
    FOR EACH STATEMENT EXECUTE PROCEDURE fn_eg_mdms_history_immutable();

CREATE TRIGGER trg_eg_mdms_data_history_immutable
    BEFORE UPDATE OR DELETE OR TRUNCATE ON eg_mdms_data_history
    FOR EACH STATEMENT EXECUTE PROCEDURE fn_eg_mdms_history_immutable();

CREATE TRIGGER trg_eg_mdms_schema_definition_history_insert_guard
    BEFORE INSERT ON eg_mdms_schema_definition_history
    FOR EACH STATEMENT EXECUTE PROCEDURE fn_eg_mdms_history_insert_guard();

CREATE TRIGGER trg_eg_mdms_data_history_insert_guard
    BEFORE INSERT ON eg_mdms_data_history
    FOR EACH STATEMENT EXECUTE PROCEDURE fn_eg_mdms_history_insert_guard();
