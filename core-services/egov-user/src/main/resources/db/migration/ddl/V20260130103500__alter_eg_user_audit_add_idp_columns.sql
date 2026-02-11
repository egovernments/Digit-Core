-- Add new authentication + MFA columns to audit table
ALTER TABLE eg_user_audit_table
    ADD COLUMN idp_issuer varchar(512),
    ADD COLUMN idp_subject varchar(512),
    ADD COLUMN idp_token_exp timestamp,
    ADD COLUMN last_sso_login_at timestamp,
    ADD COLUMN auth_provider varchar(64),
    ADD COLUMN jwt_token text,
    ADD COLUMN mfa_enabled boolean DEFAULT FALSE,
    ADD COLUMN mfa_device_name varchar(256),
    ADD COLUMN mfa_phone_last4 varchar(4),
    ADD COLUMN mfa_registered_on timestamp,
    ADD COLUMN mfa_details varchar(1000);

-- Update existing records to avoid NULL
UPDATE eg_user_audit_table
SET mfa_enabled = FALSE
WHERE mfa_enabled IS NULL;

