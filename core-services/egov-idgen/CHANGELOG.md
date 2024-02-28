# Changelog
All notable changes to this module will be documented in this file.

## 2.9.0 - 2024-02-29
- Upgraded spring boot version from 2.2.13.RELEASE to 3.2.2
- Upgraded java version from 1.8 to 17
- Upgraded postgresql version to 42.7.1
- Upgraded lombok version from 1.18.8 to 1.18.22
- Upgraded org.flywaydb:flyway-core version from 6.4.3 to 9.22.3

## 1.2.3 - 2022-01-13
- Updated to log4j2 version 2.17.1

## 1.2.2 - 2021-05-11

- TimeZone is now made configurable

## 1.2.1 - 2021-02-26

- Updated domain name in application.properties
- Added size validations
- Change the time zone to IST for date generation

## 1.2.0 - 2020-06-17

- Added typescript definition generation plugin
- Upgraded to tracer:`2.0.0-SNAPSHOT`
- Upgraded to spring boot `2.2.6-RELEASE`
- Upgraded to flyway-core `6.4.3 version`

## 1.1.0

- Added option to auto create sequences
- Moved id generation config from DB to MDMS
- Set `autocreate.new.seq` to `true` to enable auto creation of sequences

## 1.0.0

- Base version