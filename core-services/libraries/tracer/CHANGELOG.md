# Changelog
All notable changes to this library will be documented in this file.

## 2.9.3 - 2026-07-17
- Propagated correlationId and tenantId across Kafka — stamp both onto message headers on produce and rebuild the MDC from them on consume, so consumer/async logs carry the same IDs
- Added MdcRecordInterceptor (auto-attached to the default Kafka listener container factory), KafkaTemplateLoggingInterceptors, and TracerKafkaMdcUtil helper

## 2.9.2 - 2026-03-10
- Addition of Data Access Exception Handling in ExceptionAdvice.

## 2.9.0 - 2024-02-29
- Upgraded spring boot version from 2.2.6.RELEASE to 3.2.2
- Upgraded java version from 1.8 to 17

## 2.1.3
- Removed critical vulnerabilities library

## 2.1.2
- Added changes for privacy feature.

## 2.1.1
- update tenantid only if header tenant value is empty during intercept

## 2.1.0
- Tenant-id added to MDC for logging in central instance

## 2.0.0
- Upgraded to Spring Boot 2.2.6 RELASE

> Note: When upgrading to Spring Boot 2.2.6 in your libraries use the below dependency

```xml
<dependency>
    <groupId>org.egov.services</groupId>
    <artifactId>tracer</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

## 1.1.5

- Latest version