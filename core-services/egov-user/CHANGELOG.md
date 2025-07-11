# Changelog
All notable changes to this module will be documented in this file.


## 1.3.0 - 2025-05-27
- Introduced tenant-specific database schema support and dynamic schema resolution
- Refactored repositories and services to use dynamic schema placeholders and centralized schema utilities.
- Updated method signatures and constructors for tenant context and new dependencies.
- Updated test classes for new dependencies 
- Implemented multi-schema migration scripts and utilities for schema handling.

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
