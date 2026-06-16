# Changelog
All notable changes to this module will be documented in this file.

## 2.9.4 - 2026-06-16
- Added `countryCode` field to the `POST /_send` OTP request contract (`Otp.java`).
- `countryCode` is forwarded alongside `mobileNumber` when dispatching OTP SMS events to Kafka topic `egov.core.notification.sms.otp`.
- Added `MobileNumerValidationCacheRepository` — caches mobile validation configs from MDMS-v2 in Redis under key `user-otp:mobile-val:{tenantId}:{countryCode}`.
- Cache TTL configurable via `egov.validation.cache.ttl.seconds` (default 3600 s); TTL = 0 caches indefinitely.
- **Backward compatible**: `countryCode` is optional; omitting it preserves existing OTP dispatch behaviour.

## 2.9.3 - 2026-03-16
- Upgraded Spring Boot version from 3.2.2 to 3.4.5 to fix HIGH/CRITICAL CVEs
- Upgraded tracer library version
- Removed hardcoded log4j2.version override (now managed by Spring Boot)

## 2.9.0 - 2024-02-29
- Upgraded spring boot version from 2.2.13.RELEASE to 3.2.2
- Upgraded java version from 1.8 to 17
- Upgraded org.egov.services:tracer version from 2.0.0-SNAPSHOT to 2.9.0-SNAPSHOT

## 1.2.1 - 2023-08-11
- Central Instance Library Integration

## 1.2.0 - 2023-03-15

- Added support for sending otp via email by emitting email events.

## 1.1.5 - 2022-03-02

- Added genric message for failed login attempt.
- 
## 1.1.4 - 2022-01-13

- Updated to log4j2 version 2.17.1

## 1.1.3 - 2021-05-11

- Changes to error handling
- Added statelevel flag for localization

## 1.1.2 - 2021-05-04

- Added environment variable `egov.localisation.tenantid.strip.suffix.count` to get required tenantid for localisation

## 1.1.1 - 2021-02-26

- Updated domain name in application.properties

## 1.1.0 - 2020-07-14

- Upgraded to tracer `2.0.0-SNAPSHOT`
- Upgraded to spring boot `2.2.6-RELEASE`


## 1.0.0

- Base version
