

# Changelog
All notable changes to this module will be documented in this file.

## 2.9.0 - 2024-02-29
- Upgraded spring boot version from 2.2.13.RELEASE to 3.2.2
- Upgraded java version from 1.8 to 17
- Upgraded postgresql version to 42.7.1
- Upgraded lombok version from 1.18.8 to 1.18.22

## 1.1.4 - 2023-08-11
- Central Instance Library Integration

## 1.1.3 - 2022007016
- Fixed: In a multi pod cluster, the service now checks if another deployment of the service has added a new key to the 
  database,
  before throwing the key not found error. 

## 1.1.2 - 2022-01-13
- Updated to log4j2 version 2.17.1

## 1.1.1 - 2021-02-26

- Updated domain name in application.properties

## 1.1.0 - 2020-06-18

- Added typescript definition generation plugin
- Upgraded to `tracer:2.0.0-SNAPSHOT`
- Upgraded to spring boot `2.2.6-RELEASE`
- Upgraded flyway version to `6.4.3`

## 1.0.0

- Base version
