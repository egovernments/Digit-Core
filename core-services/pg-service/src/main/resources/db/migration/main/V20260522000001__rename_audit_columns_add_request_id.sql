ALTER TABLE eg_pg_transactions ADD COLUMN request_id VARCHAR(128) NULL DEFAULT NULL;

ALTER TABLE eg_pg_transactions_dump ADD COLUMN request_id VARCHAR(128) NULL DEFAULT NULL;
