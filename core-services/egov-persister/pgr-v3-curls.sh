#!/usr/bin/env bash
# ============================================================================
# Dev-trigger curls for the pgr.v3.yml persister config
# (publishes in-process PersistEvents via PersisterModulithDevController, which
#  the @ApplicationModuleListener consumes and persists through PersistService)
#
# PREREQUISITES
#   1. Tables created:        psql -h localhost -U postgres -d pgr -f pgr-v3-schema.sql
#   2. Modulith enabled:      persister.modulith.enabled=true   (already set in application.properties)
#   3. Only this config loaded. Point the persister at pgr.v3.yml and nothing else, e.g.:
#         egov.persist.yml.repo.path=classpath:pgr.v3.yml
#      (override via -Degov.persist.yml.repo.path=... or application.properties)
#   4. App running on :8082 with context-path /common-persist (JDK 17).
#
# Run the whole file:   bash pgr-v3-curls.sh
# Watch the app logs for:
#   "DEV trigger: publishing PersistEvent for topic: <topic>"
#   "Modulith listener received PersistEvent for topic: <topic>"
#   "N applicable configs found!"
# ============================================================================

BASE_URL="http://localhost:8082/common-persist"

# ---------------------------------------------------------------------------
# 1) save-pgr-service
#    INSERTs into eg_pgr_service, eg_pgr_action and eg_pgr_address.
# ---------------------------------------------------------------------------
echo "==> POST save-pgr-service"
curl -sS -X POST "${BASE_URL}/_modulith/publish?topic=save-pgr-service" \
  -H "Content-Type: application/json" \
  -d '{
    "services": [
      {
        "tenantId": "pb.amritsar",
        "serviceCode": "StreetLightNotWorking",
        "serviceRequestId": "PGR-2024-001",
        "description": "Street light not working near the park",
        "lat": 31.6340,
        "long": 74.8723,
        "addressId": "ADDR-001",
        "address": "Ranjit Avenue, Amritsar",
        "email": "citizen@example.com",
        "deviceId": "device-123",
        "accountId": "ACC-001",
        "firstName": "Test",
        "lastName": "Citizen",
        "phone": "9999999999",
        "attributes": { "priority": "high", "tags": ["light", "safety"] },
        "status": "PENDINGFORASSIGNMENT",
        "source": "web",
        "expectedTime": 1718500000000,
        "rating": 0,
        "feedback": "",
        "landmark": "Near Central Park",
        "auditDetails": {
          "createdBy": "uuid-creator",
          "createdTime": 1718400000000,
          "lastModifiedBy": "uuid-creator",
          "lastModifiedTime": 1718400000000
        },
        "addressDetail": {
          "uuid": "ADDR-UUID-001",
          "houseNoAndStreetName": "12, Mall Road",
          "mohalla": "Ranjit Avenue",
          "landmark": "Near Central Park",
          "latitude": 31.6340,
          "longitude": 74.8723,
          "city": "Amritsar",
          "tenantId": "pb.amritsar",
          "auditDetails": {
            "createdBy": "uuid-creator",
            "createdTime": 1718400000000,
            "lastModifiedBy": "uuid-creator",
            "lastModifiedTime": 1718400000000
          }
        }
      }
    ],
    "actionInfo": [
      {
        "uuid": "ACTION-UUID-001",
        "by": "uuid-creator",
        "when": 1718400000000,
        "action": "OPEN",
        "status": "PENDINGFORASSIGNMENT",
        "comments": "Complaint registered",
        "media": { "files": ["http://example.com/photo1.jpg"] },
        "assignee": "uuid-assignee",
        "isInternal": false,
        "tenantId": "pb.amritsar",
        "businessKey": "PGR-2024-001"
      }
    ]
  }'
echo

# ---------------------------------------------------------------------------
# 2) update-pgr-service
#    UPDATEs eg_pgr_service WHERE (tenantid, servicerequestid) and INSERTs a new
#    eg_pgr_action row. Run AFTER the save above so there is a row to update.
# ---------------------------------------------------------------------------
echo "==> POST update-pgr-service"
curl -sS -X POST "${BASE_URL}/_modulith/publish?topic=update-pgr-service" \
  -H "Content-Type: application/json" \
  -d '{
    "services": [
      {
        "serviceCode": "StreetLightNotWorking",
        "description": "Street light still not working - escalated",
        "lat": 31.6340,
        "long": 74.8723,
        "addressId": "ADDR-001",
        "address": "Ranjit Avenue, Amritsar",
        "email": "citizen@example.com",
        "deviceId": "device-123",
        "firstName": "Test",
        "lastName": "Citizen",
        "phone": "9999999999",
        "attributes": { "priority": "critical" },
        "status": "ASSIGNED",
        "source": "web",
        "expectedTime": 1718600000000,
        "feedback": "",
        "rating": 0,
        "landmark": "Near Central Park",
        "auditDetails": {
          "lastModifiedBy": "uuid-assignee",
          "lastModifiedTime": 1718450000000
        },
        "tenantId": "pb.amritsar",
        "serviceRequestId": "PGR-2024-001"
      }
    ],
    "actionInfo": [
      {
        "uuid": "ACTION-UUID-002",
        "by": "uuid-assignee",
        "when": 1718450000000,
        "action": "ASSIGN",
        "status": "ASSIGNED",
        "comments": "Assigned to field engineer",
        "media": {},
        "assignee": "uuid-engineer",
        "isInternal": true,
        "tenantId": "pb.amritsar",
        "businessKey": "PGR-2024-001"
      }
    ]
  }'
echo

# ---------------------------------------------------------------------------
# Verify what landed in the DB:
#   psql -h localhost -U postgres -d pgr -c "SELECT servicerequestid, status, description FROM eg_pgr_service;"
#   psql -h localhost -U postgres -d pgr -c "SELECT uuid, action, status, isinternal FROM eg_pgr_action;"
#   psql -h localhost -U postgres -d pgr -c "SELECT uuid, city, mohalla FROM eg_pgr_address;"
#
# Inspect the Spring Modulith event publication registry (the JDBC outbox).
# A row is written per (publication, listener). completion_date IS NULL means the
# listener has not finished successfully yet; a non-null value means completed.
#   psql -h localhost -U postgres -d pgr -c \
#     "SELECT id, listener_id, event_type, publication_date, completion_date FROM event_publication ORDER BY publication_date DESC;"
#
# Outstanding (incomplete) publications only — these are what get replayed on restart:
#   psql -h localhost -U postgres -d pgr -c \
#     "SELECT id, listener_id, event_type, publication_date FROM event_publication WHERE completion_date IS NULL;"
# ---------------------------------------------------------------------------
