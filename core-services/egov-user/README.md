# Egov-user service

<p>Egov-user service is used for user data management and providing functionality to login and logout into Digit system </p>

### DB UML Diagram

- NA

### Service Dependencies

- egov-mdms-service
- egov-enc-service
- egov-otp
- egov-filestore


### Swagger API Contract

http://editor.swagger.io/?url=https://raw.githubusercontent.com/egovernments/egov-services/master/docs/egov-user/contracts/v1-1-0.yml#!/

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

## v2 Bulk User Create

A dedicated bulk endpoint `POST /users/v2/_create` was added to eliminate the per-user amplification that upstream services (HRMS, project-factory) hit when creating employees in a batch. v1 endpoints are untouched and remain the default path for single-user creates.

### Overview

One request → N users. The service amortises every per-user overhead into a single batched operation:

```
POST /users/v2/_create   { RequestInfo, users: [ User, User, ... ] }
        │
        ▼
BulkUserService.createUsersBulk(list, requestInfo)
  1. sanitize + enrich          — null out server-owned fields, apply defaults
  2. bulk PII encrypt           — ONE call to egov-enc-service for the whole list
  3. bulk uniqueness check      — ONE SQL: WHERE username IN (?, ?, ...)
  4. dedup within batch         — first occurrence kept, later duplicates dropped
  5. parallel BCrypt            — CompletableFuture pool, fixed-size (default 4)
  6. batch INSERT               — jdbcTemplate.batchUpdate for eg_user + eg_userrole_v1
  7. bulk PII decrypt           — ONE call to egov-enc-service for the response
```

### Effect on downstream services

For a batch of N users the amortisation profile:

| Operation | v1 (N × single-create) | v2 bulk-create | Ratio |
|-----------|------------------------|----------------|-------|
| enc-service HTTP calls | 2N (encrypt + decrypt per user) | 2 (one encrypt, one decrypt) | **N×** |
| Uniqueness SQL queries | N | 1 | **N×** |
| BCrypt CPU time | serial on 1 Tomcat thread | parallel on `bcryptPool` threads | up to **4×** |
| DB INSERTs (eg_user + roles) | 2N | 2 batched (`batchUpdate`) | **N×** |

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `egov.user.bulk.max` | `100` | Maximum users per bulk request (413 above this) |
| `egov.user.bulk.bcrypt.threads` | `4` | Size of the BCrypt worker pool used by the bulk flow |

### Request / Response

```json
POST /users/v2/_create
{
  "RequestInfo": { ... },
  "users": [
    {
      "username": "emp_1",
      "mobileNumber": "9111234567",
      "type": "EMPLOYEE",
      "tenantId": "os.osun",
      "name": "Employee One",
      "roles": [ { "code": "CAMPAIGN_SUPERVISOR", "tenantId": "os.osun" } ]
    },
    ...
  ]
}
```

```json
{
  "ResponseInfo": { ... },
  "users": [
    { "id": 84591, "uuid": "...", "username": "emp_1", "mobileNumber": "9111234567", ... },
    ...
  ]
}
```

Users that were dropped for being duplicates (either against the DB or within the batch) are returned in the response with `id = null`; callers correlate by `username`.

### Design notes

- **One model end-to-end.** Same `User` domain object is used from controller through service to repository — no DTO layer, no contract-to-domain mappers.
- **v1 untouched.** Only addition to v1 is a new Spring `@Bean("bcryptPool")` in `EgovUserApplication`. v1 code never references it.
- **v2 owns its INSERT SQL.** `BulkUserRepository` carries its own `INSERT INTO eg_user (...)` string rather than reusing v1's `UserTypeQueryBuilder.getInsertUserQuery()`, so a schema-drift in v1's constant cannot break v2.

---

## Bulk Search Criteria

`POST /_search` and `/v1/_search` accept two new list fields alongside their scalar counterparts. When set, they generate a `WHERE ... IN (?, ?, ...)` clause instead of a single equality, and their values are encrypted in a single bulk call to enc-service.

| Field | Type | Semantics |
|-------|------|-----------|
| `userNames` | `List<String>` | `WHERE username IN (?, ?, ...)` — encrypted deterministically before the query |
| `mobileNumbers` | `List<String>` | `WHERE mobilenumber IN (?, ?, ...)` — encrypted deterministically before the query |

### Backward compatibility

- Scalar `userName` and `mobileNumber` continue to work unchanged.
- If both scalar and list forms are set for the same field, the **list wins** and the scalar is dropped with a warning log. This prevents ambiguous "AND-them-together" semantics.
- No MDMS or migration changes are required — encryption uses the existing `DataSecurity.SecurityPolicy` for `User` (deterministic on `username` and `mobileNumber`).

### Example — one call replaces N

Before (per-employee loop in HRMS):
```
for each employee in batch:
    POST /_search  { userName: "emp_i" }         → 2 enc calls, 1 SQL
Total for N=30 employees: 60 enc calls, 30 SQL queries
```

After:
```
POST /_search  { userNames: ["emp_1", "emp_2", ..., "emp_30"] }
Total: 3 enc calls (1 list-encrypt + 1 criteria-encrypt + 1 response-decrypt), 1 SQL query
```

### Request shape

```json
POST /_search
{
  "RequestInfo": { ... },
  "tenantId": "os.osun",
  "userType": "EMPLOYEE",
  "userNames": ["emp_1", "emp_2", "emp_3"]
}
```

or

```json
{
  "RequestInfo": { ... },
  "tenantId": "os.osun",
  "userType": "EMPLOYEE",
  "mobileNumbers": ["9111234567", "9222345678"]
}
```

---

## API Details

### `POST /citizen/_create`

Create a citizen with OTP validation. Mobile number and country code are validated against MDMS-v2 before creation. If `citizen.registration.withlogin.enabled=true` the citizen is logged in automatically and receives auth/refresh tokens.

### `POST /users/_createnovalidate`

Create a user without OTP validation. Mobile number and country code are still validated against MDMS-v2.

### `POST /users/v2/_create`

**Bulk** user create. Accepts up to `egov.user.bulk.max` users (default 100) per request. Amortises encryption, uniqueness check, BCrypt hashing, and INSERTs across the whole batch. Duplicates (against the DB or within the batch) are dropped and returned with `id = null`. See [v2 Bulk User Create](#v2-bulk-user-create).

### `POST /_search`

Search users by criteria. Defaults to active users only if `active` is not specified.

Also accepts bulk criteria `userNames: [...]` and `mobileNumbers: [...]` — see [Bulk Search Criteria](#bulk-search-criteria).

### `POST /v1/_search`

Same as `/_search` but returns both active and inactive users when `active` is not set. Also accepts the same bulk criteria fields.

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

### API Details


a) `POST /citizen/_create`

Create citizen with otp validation. If `citizen.registration.withlogin.enabled` property in applications.properties is `true` then created citizen would be logged in automatically and he
would get information to access platform services, ex:- auth token, refresh token etc.

b) `POST /users/_createnovalidate`

Create user without any otp validation.

c) `POST /_search`

End-point to search the users by providing userSearchRequest. In Request if there is no active filed value, it will fetch only active users.
The available search parameters are more in interservice call as compared to call coming externally.

d) `POST /v1/_search`

Similar to `/_search` endpoint except there is no default value provided for search active/inactive users.

e) `POST /_details`

End-point to fetch the user details by access-token

f) `POST /users/_updatenovalidate`

End-point to update the user details without otp validations. User's username, type and tenantId are not updated and ignored in update.

g) `POST /profile/_update`

End-point to update user profile. This allows partial update on user's account.

h) `POST /password/_update`

End-point to update the password for loggedInUser. The existing password is validated before updating new password.

i) `POST /password/nologin/_update`

End-point to update the password for non logged in user. The otp is validated before updating new password.

j) `POST /_logout`

Endpoint to logout session

k) `POST /user/oauth/token`

Endpoint for login. If the user is citizen the login is otp based else it is password based.



### Kafka Consumers
NA

### Kafka Producers
- ```audit_data``` : used in ```kafka.topic.audit``` application property, user service uses this topic for logging user data decryption calls.