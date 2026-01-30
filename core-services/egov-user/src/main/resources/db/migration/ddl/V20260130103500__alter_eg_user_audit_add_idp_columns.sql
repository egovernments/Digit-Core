ALTER TABLE eg_user_audit_table
    ADD COLUMN idp_issuer character varying(512),
    ADD COLUMN idp_subject character varying(512),
    ADD COLUMN idp_token_exp timestamp,
    ADD COLUMN last_sso_login_at timestamp,
    ADD COLUMN auth_provider character varying(64),
    ADD COLUMN jwt_token text;
