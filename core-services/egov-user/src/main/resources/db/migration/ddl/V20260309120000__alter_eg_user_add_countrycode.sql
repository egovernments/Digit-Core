-- Add country code column to eg_user table
ALTER TABLE eg_user ADD COLUMN IF NOT EXISTS countrycode character varying(10);
