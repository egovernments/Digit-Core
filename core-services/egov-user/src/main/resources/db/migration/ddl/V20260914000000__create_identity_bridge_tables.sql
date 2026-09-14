CREATE TABLE IF NOT EXISTS eg_identity_subject (
    id                  uuid PRIMARY KEY,
    issuer              character varying(512) NOT NULL,
    external_subject    character varying(255) NOT NULL,
    digit_user_uuid     character varying(64) NOT NULL,
    active              boolean NOT NULL DEFAULT true,
    created_at          bigint NOT NULL,
    updated_at          bigint NOT NULL,
    CONSTRAINT uq_identity_subject_external UNIQUE (issuer, external_subject),
    CONSTRAINT uq_identity_subject_digit_user UNIQUE (digit_user_uuid)
);

CREATE TABLE IF NOT EXISTS eg_identity_organization (
    organization_id     character varying(64) PRIMARY KEY,
    organization_alias  character varying(63) NOT NULL,
    tenant_id           character varying(256) NOT NULL,
    name                character varying(200) NOT NULL,
    active              boolean NOT NULL DEFAULT true,
    created_at          bigint NOT NULL,
    updated_at          bigint NOT NULL,
    CONSTRAINT uq_identity_organization_alias UNIQUE (organization_alias),
    CONSTRAINT uq_identity_organization_tenant UNIQUE (tenant_id)
);

CREATE TABLE IF NOT EXISTS eg_identity_membership (
    id                  uuid PRIMARY KEY,
    subject_id          uuid NOT NULL REFERENCES eg_identity_subject(id),
    organization_id     character varying(64) NOT NULL
                            REFERENCES eg_identity_organization(organization_id),
    active              boolean NOT NULL DEFAULT true,
    authorization_version bigint NOT NULL DEFAULT 1,
    created_at          bigint NOT NULL,
    updated_at          bigint NOT NULL,
    CONSTRAINT uq_identity_membership UNIQUE (subject_id, organization_id)
);

CREATE TABLE IF NOT EXISTS eg_identity_projected_role (
    membership_id       uuid NOT NULL REFERENCES eg_identity_membership(id) ON DELETE CASCADE,
    role_code           character varying(128) NOT NULL,
    CONSTRAINT pk_identity_projected_role PRIMARY KEY (membership_id, role_code)
);

CREATE INDEX IF NOT EXISTS idx_identity_membership_organization
    ON eg_identity_membership (organization_id, active);
