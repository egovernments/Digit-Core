-- Add new authentication + MFA columns
ALTER TABLE eg_user
    ADD COLUMN idp_issuer varchar(512),
    ADD COLUMN idp_subject varchar(512),
    ADD COLUMN idp_token_exp timestamp,
    ADD COLUMN last_sso_login_at timestamp,
    ADD COLUMN auth_provider varchar(64) DEFAULT 'LOCAL',
    ADD COLUMN jwt_token text,
    ADD COLUMN mfa_enabled boolean DEFAULT FALSE,
    ADD COLUMN mfa_device_name varchar(256),
    ADD COLUMN mfa_phone_last4 varchar(4),
    ADD COLUMN mfa_registered_on timestamp,
    ADD COLUMN mfa_details varchar(1000);

-- Ensure existing rows don’t remain NULL for mfa_enabled
UPDATE eg_user
SET mfa_enabled = FALSE
WHERE mfa_enabled IS NULL;

-- Add unique constraint for external identity provider mapping
ALTER TABLE eg_user
    ADD CONSTRAINT eg_user_idp_issuer_subject_key
        UNIQUE (idp_issuer, idp_subject);
