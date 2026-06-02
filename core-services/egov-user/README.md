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

Mobile validation was enhanced to support international country codes driven entirely by MDMS-v2 configuration.

### Overview

Validation is performed by `MobileNumberValidator` (injected into `UserController`) on every create/update endpoint — for both the primary mobile number and the alternate mobile number.

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
Check Redis cache
  key: "validationRules" hash
  field: "validation:{stateTenantId}:{sanitizedCountryCode}"
        │
        ├── Cache HIT ──► use cached ValidationData
        │
        ├── Cache MISS ──► call MDMS-v2 API
        │        POST {egov.mdms.v2.host}{egov.mdms.v2.search.endpoint}
        │        schemaCode = common-masters.UserValidation
        │        tenantId   = state-level tenant (first segment before ".")
        │
        │        Iterate MDMS response (isActive=true entries):
        │          1. Match attributes.prefix == countryCode ──► use this entry
        │          2. No match found ──► fall back to entry with isDefault=true
        │          3. No default entry ──► return null ──► throw VALIDATION_CONFIG_MISSING
        │
        │        Cache result in Redis (TTL: egov.validation.cache.ttl.seconds, default 3600 s)
        │
        ▼
Apply ValidationRules:
  - minLength check
  - maxLength check
  - regex pattern match (rules.pattern)
        │
        ├── Any failure ──► throw CustomException with error codes
        │
        ▼
Return attributes.prefix as the resolved/normalized countryCode
(caller sets user.countryCode = returned value)
```

### MDMS-v2 Master Data Structure

**Schema code:** `common-masters.UserValidation`

Each active entry in MDMS must follow this structure (schema and sample data are under `src/main/resources/common-masters.UserValidation.json` and `src/main/resources/common-masters.UserValidation.data.json`):

```json
{
  "tenantId": "{tenantid}",
  "schemaCode": "common-masters.UserValidation",
  "isActive": true,
  "data": {
    "fieldType": "mobileNumber",
    "zone": "IN",
    "default": true,
    "attributes": {
      "prefix": "+91"
    },
    "rules": {
      "pattern": "[6-9][0-9]{9}",
      "minLength": 10,
      "maxLength": 10,
      "errorMessage": "Mobile number must be exactly 10 digits and start with 6, 7, 8, or 9",
      "allowedStartingCharacters": ["6", "7", "8", "9"]
    }
  }
}
```

**Field descriptions:**

| Field | Type | Description |
|-------|------|-------------|
| `data.zone` | String | Unique zone identifier for the entry, e.g. `IN`, `US`, `GB` |
| `data.attributes.prefix` | String | Country dialing prefix used for matching, e.g. `+91`, `+1`, `+44` |
| `data.default` | Boolean | When `true`, this entry is used as fallback when no prefix matches |
| `data.rules.pattern` | String | Java regex applied against the local number (without country code) |
| `data.rules.minLength` | Integer | Minimum digit count |
| `data.rules.maxLength` | Integer | Maximum digit count |
| `data.rules.errorMessage` | String | Error message returned on pattern mismatch |
| `data.rules.allowedStartingCharacters` | List\<String\> | (Informational) list of valid starting digits |
| `isActive` | Boolean | Only `true` entries are considered |

### Fallback / Default Behaviour

1. **Country code provided and matched** — the matching MDMS entry's rules are applied and `attributes.prefix` is returned as the normalised country code.
2. **Country code provided but no match** — the entry with `"default": true` is used. This allows a single catch-all config (e.g. India rules) to handle unknown country codes.
3. **No country code in request** — treated as a null country code; the default entry is used.
4. **No MDMS entry found at all** — `VALIDATION_CONFIG_MISSING` error is thrown.
5. **MDMS service unavailable** — exception is caught and logged; `null` is returned, causing `VALIDATION_CONFIG_MISSING` to be thrown so the caller is informed.

### Caching

Validation configurations are cached in Redis using a hash structure:

| Property | Value |
|----------|-------|
| Redis hash key | `validationRules` |
| Hash field format | `validation:{stateTenantId}:{sanitizedCountryCode}` |
| Default TTL | `3600` seconds (configurable via `egov.validation.cache.ttl.seconds`) |
| TTL = 0 | Entries cached indefinitely |

Cache keys are keyed at the **state-level tenant** (first segment before `.`), so `pb.amritsar` and `pb.ludhiana` share the same cache entry under `pb`.

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `egov.mdms.v2.host` | `https://dev.digit.org` | MDMS-v2 host |
| `egov.mdms.v2.search.endpoint` | `/mdms-v2/v2/_search` | MDMS-v2 search path |
| `egov.mobile.validation.schema.code` | `common-masters.UserValidation` | MDMS schema code for validation rules |
| `egov.validation.cache.ttl.seconds` | `3600` | Redis cache TTL in seconds |
| `egov.user.countrycode.default` | `+91` | Default country code used internally (login/OTP flows) |
| `mobile.number.validation.workaround.enabled` | `false` | Bypass mobile validation (legacy workaround) |

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
