

# Changelog
All notable changes to this module will be documented in this file.

## 2.9.0 - 2024-02-29
- Upgraded spring boot version from 2.2.13.RELEASE to 3.2.2
- Upgraded java version from 1.8 to 17
- Upgraded lombok version from 1.18.8 to 1.18.22
- Upgraded org.egov.services:tracer version from 2.1.0-SNAPSHOT to 2.9.0-SNAPSHOT
- Upgraded org.egov:mdms-client version from 0.0.2-SNAPSHOT to 2.9.0-SNAPSHOT

## 1.3.3 - 2023-08-11
- Central Instance Library Integration

## 1.3.2 - 2022-01-13
- Updated to log4j2 version 2.17.1

## 1.3.1 - 2021-05-11
- Added finally blocked wherever missing
- Changes to error handling


## 1.3.0 - 2020-05-29

- Added typescript definition generation plugin
- Upgraded to `tracer:2.0.0-SNAPSHOT`
- Upgraded to spring boot `2.2.6-RELEASE`
- Upgraded to spring-kafka `2.3.7.RELEASE`
- Upgraded to spring-integration-kafka `3.2.0.RELEASE`
- Upgraded to jackson-dataformat-yaml `2.10.0`
- Upgraded to jackson-databind `2.10.0`
- Removed the spring-integration-java-dsl because from Spring Integration 5.0, the 
  'spring-integration-java-dsl' dependency is no longer needed. The Java DSL has 
  been merged into  the core project.

## 1.2.0

- Removed reload consumers
- Removed `start.sh` and `Dockerfile`
- Remove reload endpoints
- Remove all unused dependencies
- Migrated to the latest Spring Boot `2.2.6`
- Upgraded to tracer `2.0.0`

## 1.1.0

- Added support for partial files using `isMergeAllowed` flag in the config file. By default, merge is `false`

## 1.0.0

- Base version
