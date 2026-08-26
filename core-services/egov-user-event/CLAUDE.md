# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

egov-user-event is a Spring Boot 3.2 / **Java 25** microservice in the DIGIT core-services monorepo. It manages citizen-facing notifications ("events") — created by other modules (PT, PGR, TL), employees, or system broadcasts — and exposes create/update/search/count APIs plus a last-access-time API used to compute read/unread counts.

The REST layer implements the **3.0 contract** (`user-events-3.0.yml`): header-based tenancy/identity (`X-Tenant-ID`, `X-User-ID`), bare-array responses, spec error arrays. The internal domain model, DB schema, and inbound Kafka consumer topics are unchanged from the legacy service — the `web/contract/v3` DTOs + `web/mapper/EventApiMapper` translate at the boundary. Persistence is direct via Spring Data JPA (see Architecture). See the README's "3.0 API" section for endpoint semantics and accepted gaps (e.g. EVENTSONGROUND cannot be created via REST).

## Build & Run

```bash
mvn clean package          # build (also runs typescript-generator over web contracts)
mvn spring-boot:run        # run locally on port 8091, context path /egov-user-event
mvn test                   # unit tests (mapper + standalone-MockMvc controller tests)
mvn test -Dtest=EventApiMapperTest             # run a single test class
mvn test -Dtest=UserEventsControllerTest#createReturns201WithBareArrayAndHeaders  # single method
```

Build requires **JDK 25** (`java.version=25`). Three pom overrides exist because the Boot 3.2.2 parent predates JDK 25 — do not remove them: `lombok.version=1.18.46` + `-proc:full` compiler arg (JDK ≥23 disabled implicit annotation processing; without these Lombok silently generates nothing and you get hundreds of "cannot find symbol" errors), `mockito.version`/`byte-buddy.version` bumps, and `spring-boot-maven-plugin` pinned to 3.5.11 (older repackage cannot read class-file major 69).

Dependencies are pulled from eGov Nexus repositories (tracer, mdms-client snapshots) — network access to nexus-repo.digit.org / nexus-repo.egovernments.org is required for a clean build.

Local runs need Postgres (`jdbc:postgresql://localhost:5432/mseva`), Kafka on `localhost:9092`, and reachable egov-mdms-service and egov-localization hosts (see `LOCALSETUP.md` for the kubectl port-forward recipe; override `egov.mdms.host` / `egov.localisation.host` in `src/main/resources/application.properties`).

Flyway is **disabled** at app startup (`spring.flyway.enabled=false`). Migrations in `src/main/resources/db/migration/main` are applied by a separate Flyway Docker image (`src/main/resources/db/Dockerfile`), the standard DIGIT deployment pattern. Schema history table: `mseva_notification_schema_version`.

## Architecture

Package root: `src/main/java/org/egov/userevent/`

Two entry paths converge on `UserEventsService`:
- **REST (3.0 contract)**: `web/controller/UserEventsController` — `/v1/events/_create` (201), `_update`, `_search`; `web/controller/NotificationsController` — `/notification/_count`, `/lat/_update`. Supporting layer: `web/contract/v3/*` (API DTOs), `web/mapper/EventApiMapper` (v3 ↔ internal, incl. CANCELED↔CANCELLED status mapping and name/source defaulting), `web/mapper/EventUpdateMerger` (restores DB state for fields the 3.0 API dropped before updates — prevents NPEs and JSONB data loss), `web/context/GatewayRequestInfoFactory` (builds internal RequestInfo from headers; roles come from the bearer JWT's `realm_access.roles` claim — unverified decode, gateway owns signature verification, Keycloak plumbing roles filtered, fallback to a synthesized type-role when no token; dotless JWT roles are normalized to `R.R` in `UserEventsUtils` on both registry write and search expansion), `web/error/ApiExceptionHandler` (spec error arrays; out-orders tracer's ExceptionAdvise), `web/filter/ResponseHeaderFilter`.
- **Kafka (legacy contract)**: `consumer/UserEventsConsumer` listens on `persist-user-events-async` (create) and `update-user-events-async` (update) with the old `EventRequest`+RequestInfo payload so other services can emit events without an HTTP call.

Writes go **directly to the DB via Spring Data JPA**: `persistence/EventPersistenceService` (@Transactional) writes `persistence/entity/*` entities through `persistence/repository/*JpaRepository`. Create/update/LAT commit before the API responds (the old Kafka→egov-persister flow and `UserEventsProducer` are retired; `egov-user-event-persister.yml` in the persister module is obsolete for this service). Update semantics deliberately mirror the old persister: tenantid/source/eventtype/postedby/referenceid are immutable, the recipient registry is delete-and-reinserted per event. The jsonb columns hold the same Jackson-serialized contract objects the persister used to write, so old and new rows are interchangeable. Reads still go through `repository/UserEventRepository` (JdbcTemplate + `querybuilder`/`rowmappers`) — do not convert the dynamic search to JPA without accounting for the recipient-registry subqueries.

Key domain logic to understand before changing anything:

- **Recipient registry**: recipients are flattened into `"<type>|<role>|<tenant>"` strings (with `*` wildcards) stored in `eg_men_recepnt_event_registry`. `utils/UserEventsUtils.manageRecepients` builds this map on create/update; `buildRecepientListForSearch` generates all wildcard combinations for a searcher (e.g. a citizen matches `userUuid`, `CITIZEN|*|*`, `*|CITIZEN|*`, `*|*|<tenant>`, `ALL`). This is the core of how search targets events to users.
- **Counter events**: when an `EVENTSONGROUND` event is updated or cancelled, `UserEventsService.updateEvents` generates a "counter event" (localized message like "event X was cancelled") referencing the original via `referenceid`. Citizen search de-duplicates originals that have counter events (`citizenSearchPostProcessor`).
- **Lazy status transitions**: there is no cron. `searchPostProcessor` flips BROADCAST events ACTIVE/INACTIVE based on fromDate/toDate at search time and persists the change via an internal update (`isInternallyUpdated` flag skips audit-detail changes).
- **Role-based search enrichment**: `enrichSearchCriteria` ignores caller-supplied userIds/roles for CITIZEN users and forces their own uuid/role; SYSTEM users get a filtered "open search" result (`getFilterEventsforOpenSearch`).
- **MDMS-driven validation**: `web/validator/UserEventsValidator` validates event types/categories against MDMS masters fetched by `service/MDMSService` (module `mseva`). Counter-event text comes from `service/LocalizationService` (module `rainmaker-uc`).

Config lives in `config/PropertiesManager` (typed accessors over `application.properties`). Constants and MDMS/localization codes are in `utils/UserEventsConstants`; error codes in `utils/ErrorConstants`.

DB tables: `eg_men_events` (event + jsonb recepient/eventdetails/actions), `eg_men_recepnt_event_registry`, `eg_men_user_llt` (last-login-time, drives read/unread counts).

## Conventions & Gotchas

- API contracts in `web/contract/` are exported to TypeScript by the typescript-generator Maven plugin during `process-classes` — new contract classes should be added to the plugin's `<classes>` list in `pom.xml` if the UI needs them.
- "Recepient" (sic) is the established spelling throughout the codebase — keep it for consistency.
- Dates are epoch millis (`bigint` columns, `Long` fields). Note the UI sends end-of-day epochs, so single-day BROADCAST events have fromDate == toDate and are special-cased in `searchPostProcessor`.
- The Kafka consumer swallows exceptions after logging (`UserEventsConsumer.listen`) — failed messages are not retried or dead-lettered.
- Flyway stays disabled at startup and `spring.jpa.hibernate.ddl-auto=none` — JPA entities map the existing schema; never let Hibernate manage DDL here.
