-- clienteventid is client-generated, so it is only unique per user: scope the idempotency key
-- to (clienteventid, useruuid) so one user's event id can never mask another user's logout.
ALTER TABLE eg_user_session_logout_event DROP CONSTRAINT IF EXISTS eg_user_session_logout_event_pkey;
ALTER TABLE eg_user_session_logout_event ADD PRIMARY KEY (clienteventid, useruuid);
