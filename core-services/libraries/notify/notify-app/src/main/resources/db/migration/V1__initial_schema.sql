CREATE TABLE IF NOT EXISTS notification_config (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(255) NOT NULL,
    template_code    VARCHAR(255) NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT true,
    channels         JSONB NOT NULL,
    created_by       VARCHAR(255),
    created_time     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_time TIMESTAMP,
    CONSTRAINT uq_notification_config_tenant_template
        UNIQUE (tenant_id, template_code)
);

CREATE INDEX IF NOT EXISTS idx_notification_config_tenant
    ON notification_config (tenant_id);

CREATE TABLE IF NOT EXISTS provider_mapping (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(255) NOT NULL,
    channel          VARCHAR(50)  NOT NULL,
    country          VARCHAR(10),
    providers        JSONB NOT NULL,
    created_by       VARCHAR(255),
    created_time     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_time TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_provider_mapping_tenant_channel
    ON provider_mapping (tenant_id, channel);

CREATE TABLE IF NOT EXISTS provider (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_name    VARCHAR(255) NOT NULL UNIQUE,
    channels         JSONB NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT true,
    created_by       VARCHAR(255),
    created_time     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id VARCHAR(255) NOT NULL,
    tenant_id       VARCHAR(255) NOT NULL,
    template_code   VARCHAR(255) NOT NULL,
    recipient_ref   VARCHAR(255),
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_log_notification_id
    ON notification_log (notification_id);

CREATE TABLE IF NOT EXISTS notification_attempt (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id VARCHAR(255) NOT NULL,
    channel         VARCHAR(50)  NOT NULL,
    provider_name   VARCHAR(255) NOT NULL,
    attempt_no      INTEGER NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    reason          VARCHAR(1000),
    attempted_at    TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_attempt_notification_id
    ON notification_attempt (notification_id);
