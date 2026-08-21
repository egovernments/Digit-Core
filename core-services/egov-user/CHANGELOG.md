# Changelog
All notable changes to this module will be documented in this file.

## 1.2.9 - 2026-06-16
- Added international mobile number validation driven by MDMS-v2 (`common-masters.MobileNumberValidation` schema).
- Added `countryCode` field to user create/update contracts; stored in new `countrycode varchar(10)` column (Flyway `V20260309120000`).
- Added `MobileNumberValidator` — validates primary and alternate mobile numbers against MDMS-v2 rules with Redis caching.
- Validation regex cached in Redis at key `egov-user:mobile-val:{stateTenantId}:{countryCode}`; TTL configurable via `egov.validation.cache.ttl.seconds` (default 3600 s).
- Falls back to MDMS default entry when no country code supplied or no exact match exists; falls back to `egov.mobile.validation.default.regex` when MDMS returns no data.
- Added `mobile.number.validation.workaround.enabled` flag (default `false`) to bypass validation for legacy integrations during migration.
- **Backward compatible**: `countrycode` column is nullable; existing rows use the MDMS default entry at runtime without any data migration.

## 1.2.8 - 2023-03-15
- Added fallback to default message if user email update localization messages are not configured.
- Fixed bug where updating citizen profile causes server error.
- Fixed bug where employee details are updateable via citizen profile update API.

## 1.2.7 - 2022-02-02
- Added security fixes for user enumerration issue.
- Added size validation on user models
- Added email and sms notification feature whenever user changes email.

## 1.2.6 - 2022-01-13
- Updated to log4j2 version 2.17.1

## 1.2.5 - 2021-07-26
- Added OTHERS as one of the gender option values
- Allowed names with apostrophe symbol

## 1.2.4 - 2021-05-11
- added permanentCity in oAuth response
- added html validations on input fields
- replaced OTHER with TRANSGENDER in gender enum
- corrections to error handling
- updated LOCALSETUP.md



## 1.2.3 - 2021-02-26
- Updated domain name in application.properties
- Added size validations

## 1.2.2 - 2020-01-12
- Added field relationShip with guardian and refactoration of code.

## 1.2.1 - 2020-07-14

- Upgraded to kafka 1.3.11.RELEASE

## 1.2.0 - 2020-07-02

- Added support for encrypting user PII data

## 1.1.0 - 2020-06-19

- Added password policy for additional security

## 1.0.0

- Base version
