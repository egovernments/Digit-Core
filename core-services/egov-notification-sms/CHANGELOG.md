
# Changelog
All notable changes to this module will be documented in this file.

## 2.9.4 - 2026-06-16
- Added `countryCode` field to `SMSRequest` Kafka contract and `Sms` domain model.
- `countryCode` is carried through from the Kafka message to the outbound SMS provider call, enabling per-number country-code routing.
- **Backward compatible**: `countryCode` is optional in the Kafka message; existing producers that do not set it continue to work; the `sms.mobile.prefix` prefix behaviour is unchanged for such messages.

## 2.9.3 - 2026-03-16
- Upgraded Spring Boot version from 3.2.2 to 3.4.5 to fix HIGH/CRITICAL CVEs
- Upgraded tracer, services-common, mdms-client, enc-client library versions
- Upgraded PostgreSQL driver from 42.7.1 to 42.7.4
- Removed hardcoded log4j2.version override (now managed by Spring Boot)

## 2.9.1 - 2025-05-21
- Upgraded tracer version from 2.9.0 to 2.9.1
- added variables in application.properties required for opentelemetry

## 2.9.0 - 2024-02-29
- Upgraded spring boot version from 2.2.13.RELEASE to 3.2.2
- Upgraded java version from 1.8 to 17
- Upgraded lombok version from 1.18.8 to 1.18.22

## 1.2.0 - 15-03-2023

- Added new API for sms bounce-back tracking 

## 1.1.3 - 15-12-2021

- Added flag for sms enable

## 1.1.2 - 11-05-2021

- added size validations

## 1.1.1 - 26-02-2021

- Updated hashing algorithm

## 1.1.0 - 26-06-2020

- Implemented generic interface
- Upgraded to Spring Boot `2.2.6-RELEASE`
- Upgraded to `tracer:2.0.0-SNAPSHOT`
- Added `category` to SMS class
- Added `expiryTime` to SMS class
- Add kafka topics for `backup`, `expiry`, `error`

## 1.0.0

- Base version  
