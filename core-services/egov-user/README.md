# Egov-user service

Egov-user service manages user data and provides login/logout functionality for the DIGIT system.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 8 |
| Framework | Spring Boot 1.5.22 |
| Security | Spring Security OAuth2 |
| Database | PostgreSQL (via Spring JDBC) |
| Cache | Redis (Spring Data Redis) |
| Messaging | Apache Kafka |
| Encryption | egov-enc-service (via enc-client 2.0.1) |
| Master Data | MDMS-v2 |
| Build | Maven |
| Utilities | Lombok, Jackson, Flyway, joda-time |

---

## Service Dependencies

| Service | Purpose |
|---------|---------|
| `egov-mdms-service` | Role lookup (MDMS v1) |
| `mdms-v2` | Mobile number validation rules (MDMS v2) |
| `egov-enc-service` | Encrypt/decrypt PII fields (mobile, name, etc.) |
| `egov-otp` | OTP generation and validation |
| `egov-filestore` | Profile photo storage |

---

## Swagger API Contract

http://editor.swagger.io/?url=https://raw.githubusercontent.com/egovernments/egov-services/master/docs/egov-user/contracts/v1-1-0.yml#!/

---

## Service Details

### Feature List

**Employee:**
- User registration
- Search user
- Update user details
- Forgot password / Change password
- User role mapping (single ULB to multiple roles)
- Password-based login

**Citizen:**
- Create / Update / Search user
- OTP-based registration
- OTP-based login

---

## Mobile Number Validation with Country Code

Mobile validation was enhanced to support international country codes driven by MDMS-v2 (`common-masters.MobileNumberValidation` schema).

### Overview

Validation is performed by `MobileNumberValidator` on every create/update endpoint — for both the primary and alternate mobile numbers.

### Validation Flow

```
Request (mobileNumber + countryCode)
        │
        ▼
MobileNumberValidator.validateMobileNumberWithCountryCode()
        │
        ├── mobileNumber blank? ──► skip (mobile is optional)
        │
        ▼
Derive stateTenantId (first segment of tenantId before ".")

Check Redis cache
  key: "egov-user:mobile-val:{stateTenantId}:{sanitizedCountryCode}"
       (suffix = "default" when countryCode is null/empty)
        │
        ├── Cache HIT ──► use cached mobileNumberRegex string
        │
        ├── Cache MISS ──► call MDMS-v2 API
        │        POST {egov.mdms.v2.host}{egov.mdms.v2.search.endpoint}
        │        schemaCode = common-masters.MobileNumberValidation
        │        tenantId   = incoming tenantId
        │
        │        MDMS entry selection (isActive=true entries):
        │          1. data.countryCode == request countryCode ──► use mobileNumberRegex
        │          2. no exact match or null ──► use entry where data.default == true
        │          3. no default found ──► use egov.mobile.validation.default.regex
        │
        │        If no result AND tenantId != stateTenantId ──► retry with stateTenantId
        │        If still no result ──► use egov.mobile.validation.default.regex
        │
        │        Cache mobileNumberRegex string with TTL egov.validation.cache.ttl.seconds
        │
        ▼
Apply regex: mobileNumber.matches(mobileNumberRegex)
        │
        ├── failure ──► CustomException INVALID_MOBILE_NUMBER
        │
        ▼
Return countryCode (null input returns egov.mobile.validation.default.country.code)
```

### MDMS-v2 Schema Definition

**Schema code:** `common-masters.MobileNumberValidation`

Register this schema once per environment in MDMS-v2:

```json
{
  "tenantId": "{tenantid}",
  "code": "common-masters.MobileNumberValidation",
  "description": "Mobile Number Validation Configuration",
  "isActive": true,
  "definition": {
    "$schema": "http://json-schema.org/draft-07/schema#",
    "title": "Mobile Number Validation",
    "type": "object",
    "required": ["countryCode", "mobileNumberRegex"],
    "x-unique": ["countryCode"],
    "properties": {
      "countryCode":       { "type": "string" },
      "mobileNumberRegex": { "type": "string" },
      "default":           { "type": "boolean", "default": false }
    },
    "x-ref-schema": [],
    "additionalProperties": false
  }
}
```

### Sample Master Data

One data record per supported country code. Mark exactly one record `"default": true` as the catch-all:

```json
[
  {
    "tenantId": "pg",
    "data": {
      "countryCode": "+91",
      "mobileNumberRegex": "^[6-9][0-9]{9}$",
      "default": true
    }
  }
]
```

**Field descriptions:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `data.countryCode` | String | Yes | International dialling prefix, e.g. `+91`, `+1`, `+44` |
| `data.mobileNumberRegex` | String | Yes | Java regex applied to the local number (without country code) |
| `data.default` | Boolean | No | When `true`, used when no exact `countryCode` match is found; exactly one record should be `true` |

### Fallback / Default Behaviour

1. **Country code provided and matched** — the matching entry's `mobileNumberRegex` is applied and the input `countryCode` is returned normalised.
2. **Country code provided but no exact match** — the entry with `"default": true` is used as a catch-all.
3. **No country code in request** — treated as null; the default entry is used.
4. **No MDMS data at all** — falls back to `egov.mobile.validation.default.regex` from `application.properties`.
5. **MDMS unavailable** — exception is caught and logged; service falls back to `egov.mobile.validation.default.regex`.

### Caching

Each (tenant, countryCode) pair is stored as an individual Redis string key whose value is the `mobileNumberRegex`. Keys expire independently — a pod restart does not clear the cache.

| Property | Value |
|----------|-------|
| Key pattern | `egov-user:mobile-val:{stateTenantId}:{sanitizedCountryCode}` |
| No-code suffix | `default` (when countryCode is null/empty) |
| Cached value | `mobileNumberRegex` string |
| Default TTL | `3600` s (configurable via `egov.validation.cache.ttl.seconds`) |
| TTL = 0 | Cached indefinitely |

Cache is scoped to **state-level tenant** so `pg.amritsar` and `pg.citya` share the same entry under `pg`.

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `egov.mdms.v2.host` | `https://dev.digit.org` | MDMS-v2 host |
| `egov.mdms.v2.search.endpoint` | `/mdms-v2/v2/_search` | MDMS-v2 search path |
| `egov.mobile.validation.schema.code` | `common-masters.MobileNumberValidation` | MDMS schema code |
| `egov.mobile.validation.default.country.code` | `+91` | Returned when request carries no countryCode |
| `egov.mobile.validation.default.regex` | `^[6-9][0-9]{9}$` | Fallback regex when MDMS has no matching data |
| `egov.validation.cache.ttl.seconds` | `3600` | Redis cache TTL in seconds (0 = indefinite) |
| `mobile.number.validation.workaround.enabled` | `false` | Bypass `MobileNumberValidator` entirely (legacy escape hatch) |

---

## DB Schema — Mobile Number Columns

The `eg_user` table holds four mobile-related columns. Their current state and migration history is summarised below.

### Current column definitions

| Column | Type | Nullable | Added by migration |
|--------|------|----------|--------------------|
| `mobilenumber` | `varchar(150)` | YES | Initial table (expanded from `varchar(50)` by `V20190313165702`) |
| `altcontactnumber` | `varchar(150)` | YES | Initial table (expanded from `varchar(50)` by `V20190313165702`) |
| `alternatemobilenumber` | `varchar(50)` | YES | `V20210908231720` |
| `countrycode` | `varchar(10)` | YES | `V20260309120000` |

### Migration history (mobile-related)

| Migration file | Change |
|----------------|--------|
| `V20170223150524` | Created `eg_user` with `mobilenumber varchar(50)` and `altcontactnumber varchar(50)` |
| `V20170823203553` | Dropped `NOT NULL` constraint on `mobilenumber` — mobile is optional |
| `V20190313165702` | Widened `mobilenumber` and `altcontactnumber` to `varchar(150)` to accommodate encrypted values |
| `V20210908231720` | Added `alternatemobilenumber varchar(50)` — second mobile contact per user |
| `V20260309120000` | Added `countrycode varchar(10)` — stores the normalised dialling prefix (e.g. `+91`) |

### MDMS-v2 master data

Mobile validation rules are not stored in the DB; they live in the `common-masters.MobileNumberValidation` MDMS-v2 schema. Each active record holds the `mobileNumberRegex` and the `countryCode` prefix for one calling zone. See [MDMS-v2 Schema Definition](#mdms-v2-schema-definition) above for the full reference.

---

## Backward Compatibility

| Concern | Behaviour |
|---------|-----------|
| `countrycode` column is `NULL` for all existing rows | The service treats `NULL` as the MDMS default entry at runtime — no data-migration script is required. The resolved code is written back on the next update of that user. |
| API callers that do not send `countryCode` | `countryCode` defaults to `null`; the MDMS default entry is applied, preserving prior validation behaviour exactly. |
| `mobile.number.validation.workaround.enabled=true` | Completely bypasses `MobileNumberValidator`. Use only as a temporary escape hatch during migration — revert to `false` once all callers are updated. |
| `mobilenumber` field absent from request | Mobile is optional (`NOT NULL` dropped in 2017); callers that omit it are unaffected. |
| `alternatemobilenumber` null in older records | `MobileNumberValidator` skips validation when the field is blank. |
| MDMS unavailable or has no matching entry | Falls back to `egov.mobile.validation.default.regex` — validation never silently passes an invalid number. |

---

## API Details

### `POST /citizen/_create`

Create a citizen with OTP validation. Mobile number and country code are validated against MDMS-v2 before creation. If `citizen.registration.withlogin.enabled=true` the citizen is logged in automatically and receives auth/refresh tokens.

### `POST /users/_createnovalidate`

Create a user without OTP validation. Mobile number and country code are still validated against MDMS-v2.

### `POST /_search`

Search users by criteria. Defaults to active users only if `active` is not specified.

### `POST /v1/_search`

Same as `/_search` but returns both active and inactive users when `active` is not set.

### `POST /_details`

Fetch user details by access token.

### `POST /users/_updatenovalidate`

Update user details without OTP validation. Mobile number and country code are validated against MDMS-v2. `username`, `type`, and `tenantId` are ignored in update.

### `POST /profile/_update`

Partial update of user profile. Mobile and alternate mobile are validated against MDMS-v2.

### `POST /password/_update`

Update password for a logged-in user. Validates existing password before applying new one.

### `POST /password/nologin/_update`

Update password for a non-logged-in user. OTP is validated before updating.

### `POST /_logout`

Invalidate the current session.

### `POST /user/oauth/token`

Login endpoint. Citizens use OTP-based login; employees use password-based login.

---

## DB UML Diagram

NA

## Kafka Consumers

NA

## Kafka Producers

| Topic | Property | Description |
|-------|----------|-------------|
| `audit_data` | `kafka.topic.audit` | Logs user data decryption audit events |
