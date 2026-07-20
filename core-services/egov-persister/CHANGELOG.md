
# Changelog
All notable changes to this module will be documented in this file.

## 2.9.4 - 2026-07-20
- At-least-once delivery: manual offset commit (`spring.kafka.consumer.enable-auto-commit=false`), per-record (RECORD) ack on the single container and per-batch (BATCH) ack on the batch container — offsets commit only after durable handling
- SQLSTATE-based failure classification: benign duplicate (`unique_violation` 23505) treated as idempotent success, transient failures (connection/deadlock/serialization) retried in place, permanent/bad-data records routed to the dead-letter topic
- Dead-letter topic (`tracer.errorsTopic`, default `egov-persister-deadletter`) with a bounded reprocessor and a terminal parking topic (`egov-persister-deadletter-processed`); durable DLQ/parking publishes (`acks=all`, idempotent producer)
- DB-health pause/resume monitor: the single container is paused while the datasource is unreachable and resumed on recovery
- Per-record poison isolation: a failing bulk (bare JSON array) message is split so only the offending record is dead-lettered while its siblings commit
- Batch persist optimization: rows aggregated per QueryMap across all messages into a single order-preserving `batchUpdate`
- Idempotent service configs: added `ON CONFLICT (uuid) DO NOTHING` to inserts to support safe redelivery / DLQ replay
- New config keys: `persister.batch.topics`, `persister.dead-letter.*`, `persister.db-health.check-interval-ms`, `persister.custom.executor.*`, `persister.batch.parallel-topic-processing.thread-pool-size`, and the live-read `persister.kafka.*` consumer tuning knobs

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
- Upgraded postgresql version to 42.7.1
- Upgraded lombok version from 1.18.8 to 1.18.22
- Upgraded org.flywaydb:flyway-core version from 6.4.3 to 9.22.3

## 1.1.6 - 2023-08-11
- Central Instance Library Integration

## 1.1.5 - 2023-03-31
- Added code to support Persister's integration with Signed Audit Service.

## 1.1.4 - 2022-01-13
- Updated to log4j2 version 2.17.1

## 1.1.3 - 2021-05-11

-  Added finally block wherever required

## 1.1.2 - 2021-01-15

-  Add readme for persister versioning 

## 1.1.1 - 2020-10-09

- Persister: Adding Deserialization Error Handler in Persister

## 1.1.0 - 2020-06-17

- Added typescript definition generation plugin
- Upgraded to tracer:2.0.0-SNAPSHOT
- Upgraded to spring boot 2.2.6-RELEASE
- Upgraded to flyway-core 6.4.3 version
- Removed `start.sh` and `Dockerfile`
- Set autocreate.new.seq to true to enable auto creation of sequences
- Modified kafka listner containerProperty package.

## 1.0.0

- Base version
