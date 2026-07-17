# Changelog
All notable changes to this module will be documented in this file.

## 1.0.5 - 2026-07-17
- Forwarded the resolved tenantId (and correlationId) to downstream services by rebuilding the exchange in RequestEnrichmentFilter — a bare request mutate was silently discarded by Spring Cloud Gateway
- Resolved tenantId for tracing from the query param or request body, so it propagates on non-central-instance environments
- Added feature flag `egov.gateway.tenant.propagation.enabled` (default true) to toggle tenantId forwarding

## 1.0.4 - 2026-05-04
- Handled GET & form content type separately in all necessary filters

## 1.0.3 - 2026-03-16
- Upgraded Spring Boot version from 3.2.2 to 3.4.5 to fix HIGH/CRITICAL CVEs
- Upgraded tracer, services-common library versions
- Upgraded spring-cloud-starter-gateway from 4.1.1 to 4.1.5
- Removed hardcoded log4j2.version override (now managed by Spring Boot)

## 2.9.1 - 2025-05-21
- Upgraded tracer version from 2.9.0 to 2.9.1
- added variables in application.properties required for opentelemetry

## 2.9.1-HOTFIX - 2024-05-28
- Fixed support for form data

