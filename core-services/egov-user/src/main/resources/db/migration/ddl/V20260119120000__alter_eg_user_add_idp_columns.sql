ALTER TABLE eg_user
    ADD COLUMN idp_issuer character varying(512),
    ADD COLUMN idp_subject character varying(512),
    ADD COLUMN idp_token_exp timestamp,
    ADD COLUMN last_sso_login_at timestamp;

ALTER TABLE eg_user
    ADD CONSTRAINT eg_user_idp_issuer_subject_key UNIQUE (idp_issuer, idp_subject);

