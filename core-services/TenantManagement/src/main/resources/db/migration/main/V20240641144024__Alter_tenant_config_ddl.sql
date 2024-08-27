ALTER TABLE tenant_config
ADD COLUMN name VARCHAR(255);

ALTER TABLE tenant_config
ADD COLUMN otpLength VARCHAR(10);

ALTER TABLE tenant_config
ADD COLUMN languages JSONB;