

# Changelog
All notable changes to this module will be documented in this file.

## 2.9.1-HOTFIX - 2024-05-28
- Added missing underscore character in the table name in EscalationQueryBuilder.java & EscalationQueryBuilderTest.java

## 2.9.0 - 2024-02-29
- Upgraded spring boot version from 2.7.5 to 3.2.2
- Upgraded java version from 1.8 to 17
- Upgraded postgresql version from 42.2.2.jre7 to 42.7.1
- Upgraded org.egov.services:tracer version from 2.1.2-SNAPSHOT to 2.9.0-SNAPSHOT
- Upgraded org.egov:mdms-client from 0.0.2-SNAPSHOT to 2.9.0-SNAPSHOT
- Upgraded org.flywaydb:flyway-core version to 9.22.3

## 1.3.1 - 2023-08-11
- Central Instance Library Integration

## 1.3.0 - 2023-03-15
- Separated out v1 and v2 business service APIs to maintain backward compatibility.
- Added state level fallback on business service v2 search API.
- Upgraded to spring-boot version 2.7.5
- Upgraded to kafka version 3.1.1
- Upgraded to spring-beans version 5.3.23
- Upgraded to cache2k-spring version 2.6.1.Final
- Enhanced workflow service to send descriptive errors for bad requests.

## 1.2.1 - 2022-01-13
- Updated to log4j2 version 2.17.1

## 1.2.0 2021-06-23
- Added autoescalation feature
- Added statelevel fallback feature at businessService level

## 1.1.5 2021-05-11
- Inbox and processInstance count for each status

## 1.1.4 2021-03-17
- Bug fix for rate action validation

## 1.1.3 2021-02-26
- Updated domain name in application.properties
- Added rating field in processInstance
- Added uuid validation on assignes in case of CITIZEN

## 1.1.2 2021-01-11
- Query Optimization
- Caching

## 1.1.1 2020-11-13
- Added _count API
- Added pagination in default wf search

## 1.1.0 - 2020-09-01
- Added index on assignee 

## 1.1.0 - 2020-06-17
- Added typescript definition generation plugin
- Upgraded to `tracer:2.0.0-SNAPSHOT`
- Upgraded to spring boot `2.2.6-RELEASE`
- Deleted `Dockerfile` and `start.sh` as it is no longer in use

## 1.0.0

- Base version
