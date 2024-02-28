# Changelog
All notable changes to this module will be documented in this file.

## 2.9.0 - 2024-02-29
- Upgraded spring boot version from 2.2.13.RELEASE to 3.2.2
- Upgraded java version from 1.8 to 17
- Upgraded postgresql version from 9.4.1212 to 42.7.1
- Upgraded lombok version from 1.18.8 to 1.18.22
- Upgraded org.flywaydb:flyway-core version from 6.4.3 to 9.22.3
- Upgraded org.egov.services:tracer version from 2.0.0-SNAPSHOT to 2.9.0-SNAPSHOT

## 1.2.3 - 2023-03-15
- Removed Database timezone dependency

## 1.2.2 - 2022-01-13
- Updated to log4j2 version 2.17.1

## 1.2.1 - 2021-05-11

- Added size validation.

## 1.2.0 - 2020-06-16

- Added typescript definition generation plugin
- Upgraded to tracer `2.0.0-SNAPSHOT`
- Upgraded to spring boot `2.2.6-RELEASE`
- Upgraded to flyway-core `6.4.3 version`

## 1.1.0

- Added encryption of generated OTP
- `egov.otp.encrypt` can be used to disable encryption. Default and recommended value is `true`
- OTP validation will validate any valid OTP

## 1.0.0

- Base version
