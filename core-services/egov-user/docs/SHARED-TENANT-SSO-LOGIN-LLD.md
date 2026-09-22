# Shared-Tenant SSO Login — LLD

> **Amended 2026-09-22 after e2e:** mapping key is the shared-tenant-encrypted username, rows keyed by `userid` with `active` flag. Slice 1 and 3.2 below are superseded by the code as implemented: `UserTenantMappingQueryBuilder`, `UserTenantMappingRepository`, `EncryptionDecryptionUtil.tenantMappingKey`, `UserService.createUser`, `TenantLookupService`.

Binding for implementation. Design: `SHARED-TENANT-SSO-LOGIN-DESIGN.md`. Service: `egov-user` 1.3.1-oidc-SNAPSHOT, Spring Boot 1.5.22, Java 8 source, JUnit 4 + Mockito (`@RunWith(MockitoJUnitRunner.class)`). Package root `org.egov.user`. No code comments except a single-line one for a non-obvious constraint.

## Slice 1 — Mapping table, repository, write hooks

### 1.1 Migration `src/main/resources/db/migration/public/V20260922080000__create_eg_user_tenant_mapping.sql`

```sql
CREATE TABLE IF NOT EXISTS public.eg_user_tenant_mapping (
    username    character varying(180) NOT NULL,
    type        character varying(50)  NOT NULL,
    tenantid    character varying(256) NOT NULL,
    userid      bigint                 NOT NULL,
    uuid        character varying(36),
    createddate timestamp              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT eg_user_tenant_mapping_pkey PRIMARY KEY (username, type, tenantid)
);
CREATE INDEX IF NOT EXISTS idx_eg_user_tenant_mapping_username_type
    ON public.eg_user_tenant_mapping (username, type);
```

### 1.2 Migration job

- `src/main/resources/db/Dockerfile`: add `COPY ./migration/public /flyway/public`.
- `src/main/resources/db/migrate.sh`: before the schema loop add one run against `public` with `-locations=filesystem:/flyway/public -table=${SCHEMA_TABLE}_public`, only when `/flyway/public` contains files. Keep the loop unchanged.

### 1.3 `repository/builder/UserTenantMappingQueryBuilder.java`

```java
public final class UserTenantMappingQueryBuilder {
    private UserTenantMappingQueryBuilder() {}
    public static final String TABLE = "public.eg_user_tenant_mapping";
    public static final String UPSERT =
        "INSERT INTO " + TABLE + " (username, type, tenantid, userid, uuid) " +
        "VALUES (:username, :type, :tenantid, :userid, :uuid) " +
        "ON CONFLICT (username, type, tenantid) DO UPDATE SET userid = EXCLUDED.userid, uuid = EXCLUDED.uuid";
    public static final String DELETE =
        "DELETE FROM " + TABLE + " WHERE username = :username AND type = :type AND tenantid = :tenantid";
    public static final String FIND_BY_USERNAME_AND_TYPE =
        "SELECT tenantid, userid, uuid FROM " + TABLE + " WHERE username = :username AND type = :type ORDER BY tenantid";
}
```

No `{schema}` placeholder anywhere in this class.

### 1.4 `domain/model/UserTenantMapping.java`

Lombok `@Getter @Builder @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode`: `String tenantId; Long userId; String uuid;`

### 1.5 `persistence/repository/UserTenantMappingRepository.java`

```java
@Repository
public class UserTenantMappingRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    public UserTenantMappingRepository(NamedParameterJdbcTemplate t) { ... }
    public void upsert(String username, UserType type, String tenantId, Long userId, String uuid)
    public void delete(String username, UserType type, String tenantId)
    public List<UserTenantMapping> findByUsernameAndType(String username, UserType type)
}
```

- `type` param bound as `type.name()`.
- `findByUsernameAndType` uses a `RowMapper` lambda; returns empty list when none.

### 1.6 Hooks in `persistence/repository/UserRepository.java`

- Add constructor param `UserTenantMappingRepository userTenantMappingRepository` (append last; update all `new UserRepository(...)` calls in tests).
- In `create(User user)` after `final User savedUser = save(user);`:
  `if (Boolean.TRUE.equals(savedUser.getActive())) userTenantMappingRepository.upsert(savedUser.getUsername(), savedUser.getType(), savedUser.getTenantId(), savedUser.getId(), savedUser.getUuid());`
- In `update(User user, User oldUser, Long userId, String uuid)` at the end of the method, after the existing SQL update:
  ```java
  Boolean newActive = user.getActive();
  if (newActive != null && !newActive.equals(oldUser.getActive())) {
      if (newActive) userTenantMappingRepository.upsert(oldUser.getUsername(), oldUser.getType(), tenantId, userId, oldUser.getUuid());
      else userTenantMappingRepository.delete(oldUser.getUsername(), oldUser.getType(), tenantId);
  }
  ```
  `tenantId` is the local variable already computed at the top of `update`.

### 1.7 Tests

- `src/test/java/org/egov/user/persistence/repository/UserTenantMappingRepositoryTest.java`: mock `NamedParameterJdbcTemplate`; verify `upsert` binds 5 params with `type.name()`; `delete` binds 3; `findByUsernameAndType` maps rows and returns `[]` on empty.
- `UserRepositoryTest.java` (existing): add cases `create_activeUser_writesMapping`, `create_inactiveUser_skipsMapping`, `update_activeToInactive_deletesMapping`, `update_inactiveToActive_upsertsMapping`, `update_activeUnchanged_noMappingCall`, `update_activeNull_noMappingCall`.

## Slice 2 — Config, constants, errors

### 2.1 `config/OidcConfigConstants.java`

```java
public static final String KEY_USERNAME_CLAIM_KEY = "usernameClaimKey";
public static final String KEY_JIT_ENABLED = "jitEnabled";
public static final String DEFAULT_USERNAME_CLAIM_KEY = "preferred_username";
public static final boolean DEFAULT_JIT_ENABLED = false;
```

### 2.2 `config/AuthProperties.java`

- `Oidc`: add `private String sharedLoginTenantId;` (property `auth.oidc.shared-login.tenant-id`).
- `Provider`: add final fields `String usernameClaimKey` (default `DEFAULT_USERNAME_CLAIM_KEY`) and `boolean jitEnabled` (default `DEFAULT_JIT_ENABLED`); append both to the full constructor (last two params), the `Builder` fields, builder methods `usernameClaimKey(String)`, `jitEnabled(boolean)`, and `build()`. Fix the other constructor at ~line 166 to set defaults.

### 2.3 `config/MdmsOidcProviderSupplier.mapNodeToProvider`

```java
if (n.has(KEY_USERNAME_CLAIM_KEY)) builder.usernameClaimKey(n.get(KEY_USERNAME_CLAIM_KEY).asText(DEFAULT_USERNAME_CLAIM_KEY));
if (n.has(KEY_JIT_ENABLED)) builder.jitEnabled(n.get(KEY_JIT_ENABLED).asBoolean(DEFAULT_JIT_ENABLED));
```

### 2.4 `application.properties`

Under the OIDC block: `auth.oidc.shared-login.tenant-id=` (empty default; set per deployment via the `AUTH_OIDC_SHARED_LOGIN_TENANT_ID` env, Spring relaxed binding)

### 2.5 `security/oauth2/custom/jwt/SsoErrorCodes.java`

```java
public static final String TENANT_NOT_SHARED = "sso.param.tenant_not_shared";
public static final String USERNAME_CLAIM_MISSING = "sso.param.username_claim_missing";
public static final String USER_NOT_ONBOARDED = "sso.user.not_onboarded";
```

### 2.6 `domain/exception/sso/SsoMissingParamException.java`

Add factories `tenantNotShared(String tenantId)` (code `TENANT_NOT_SHARED`, message `"tenantId must be the shared login tenant"`) and `usernameClaimMissing(String claimKey)` (code `USERNAME_CLAIM_MISSING`, message `"claim <key> is absent from the id_token"`). Both `HttpStatus.BAD_REQUEST` via existing constructor.

### 2.7 `domain/exception/sso/SsoUserNotOnboardedException.java` (new)

`extends SsoException`, constructor `(String tenantId)` → code `USER_NOT_ONBOARDED`, message `"user is not onboarded in tenant <tenantId>"`, `HttpStatus.UNAUTHORIZED`.

### 2.8 Tests

- `AuthPropertiesTest` (new or existing): builder defaults for the two fields; explicit values retained.
- `MdmsOidcProviderSupplierTest` (existing if present, else new focused test on `mapNodeToProvider` via reflection or package-private access): JSON with and without the two keys.

## Slice 3 — Tenants endpoint

### 3.1 `web/contract/auth/TenantLookupResponse.java`

Lombok `@Getter @Builder`: `String username; List<UserTenantMapping> tenants;`

### 3.2 `domain/service/TenantLookupService.java`

```java
@Service
public class TenantLookupService {
    public TenantLookupService(JwtValidationService jwtValidationService,
                               OidcProviderSupplier oidcProviderSupplier,
                               UserTenantMappingRepository mappingRepository,
                               AuthProperties authProperties) {}

    public TenantLookupResponse lookup(String assertion, String tenantId) {
        String shared = authProperties.getOidc().getSharedLoginTenantId();
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(shared) || !shared.equals(tenantId))
            throw SsoMissingParamException.tenantNotShared(tenantId);
        OidcValidatedJwt jwt = jwtValidationService.validate(assertion, tenantId);
        AuthProperties.Provider provider = oidcProviderSupplier.getProviders().stream()
            .filter(p -> jwt.getProviderId().equals(p.getId()) && tenantId.equals(p.getTenantId()))
            .findFirst().orElseThrow(() -> OidcProviderConfigException.providerNotFound(jwt.getProviderId()));
        Object claim = jwt.getClaims().get(provider.getUsernameClaimKey());
        if (claim == null || !StringUtils.hasText(claim.toString()))
            throw SsoMissingParamException.usernameClaimMissing(provider.getUsernameClaimKey());
        String username = claim.toString().trim();
        List<UserTenantMapping> tenants = mappingRepository.findByUsernameAndType(username, UserType.fromValue(provider.getUserType()));
        return TenantLookupResponse.builder().username(username).tenants(tenants).build();
    }
}
```

### 3.3 `web/controller/TenantLookupController.java`

```java
@RestController
@RequestMapping("/oauth")
@ConditionalOnProperty(name = "auth.oidc.enabled", havingValue = "true")
public class TenantLookupController {
    @PostMapping(value = "/tenants", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<TenantLookupResponse> tenants(@RequestParam("assertion") String assertion,
                                                        @RequestParam(value = "tenantId", required = false) String tenantId)
}
```

Path resolves to `/user/oauth/tenants` under the service context path. Errors: `SsoException` → `ResponseEntity.status(e.getHttpStatus()).body(Map.of("error", e.getErrorCode(), "error_description", e.getMessage()))` via a `@ExceptionHandler(SsoException.class)` in this controller (Java 8: build the map with `HashMap`).

### 3.4 `security/ClientBasicAuthFilter.java`

```java
@Component
public class ClientBasicAuthFilter extends OncePerRequestFilter {
    public ClientBasicAuthFilter(ClientDetailsService clientDetailsService) {}
    protected boolean shouldNotFilter(HttpServletRequest r) { return !r.getServletPath().endsWith("/oauth/tenants"); }
    protected void doFilterInternal(...) {
        // parse Authorization: Basic base64(clientId:secret); missing/malformed → 401 {"error":"unauthorized_client"}
        // clientDetailsService.loadClientByClientId(clientId); ClientRegistrationException → 401
        // secret compared only when the client has a non-empty secret
    }
}
```

Register with a `FilterRegistrationBean` in `SecurityConfig` on URL pattern `/oauth/tenants` (order `Ordered.HIGHEST_PRECEDENCE + 10`), or rely on `@Component` auto-registration with `shouldNotFilter` as above. Pick one; do not do both.

### 3.5 Tests

- `TenantLookupServiceTest`: wrong tenant → `TENANT_NOT_SHARED`; blank configured shared tenant → same; no provider for `providerId+tenant` → `OidcProviderConfigException`; claim missing → `USERNAME_CLAIM_MISSING`; claim key override honoured; happy path returns sorted list from repository; empty list passthrough.
- `TenantLookupControllerTest` (standalone `MockMvc`): 200 body shape; `SsoException` → status and error code from the exception.
- `ClientBasicAuthFilterTest`: no header → 401; unknown client → 401; known client, blank secret → passes chain; non-tenants path → chain untouched.

## Slice 4 — Exchange changes

### 4.1 `security/oauth2/custom/jwt/JwtExchangeAuthenticationProvider.java`

Replace `findOrCreateUser` and adjust `findExistingUserAndUpdate`:

```java
private UserAndRequestInfo findOrCreateUser(OidcValidatedJwt jwt, AuthProperties.Provider provider,
        TokenMfaDetails mfaDetails, String tenantId) {
    UserType type = UserType.fromValue(jwt.getUserType());
    User user;
    try {
        user = userService.getUniqueUser(jwt.getIssuer(), jwt.getExternalUserId(), tenantId, type);
    } catch (UserNotFoundException bySubject) {
        user = findByUsernameClaim(jwt, provider, tenantId, type);
    }
    if (user == null) {
        if (!provider.isJitEnabled()) throw new SsoUserNotOnboardedException(tenantId);
        return createNewUser(jwt, provider, mfaDetails, tenantId);
    }
    return updateExistingUser(user, jwt, provider, mfaDetails, tenantId);
}

private User findByUsernameClaim(OidcValidatedJwt jwt, AuthProperties.Provider provider, String tenantId, UserType type) {
    Object claim = jwt.getClaims().get(provider.getUsernameClaimKey());
    if (claim == null || !StringUtils.hasText(claim.toString())) return null;
    try {
        return userService.getUniqueUser(claim.toString().trim(), tenantId, type);
    } catch (UserNotFoundException e) {
        return null;
    }
}
```

`updateExistingUser` is the old `findExistingUserAndUpdate` body minus the initial lookup, with this change: always call `ssoUserPersistenceService.updateUserAndUpsertIdpDetails(userForUpdate, idpDetails, tenantId, requestInfo)` when `user.getIdpSubject() == null || !jwt.getSubject().equals(user.getIdpSubject())` (first link); otherwise `upsertIdpDetailsOnly` + decrypt path as today. Remove the `rolesChanged` call and the `rolesChanged` method if now unused.

`createUserForSsoUpdate`: delete the `.roles(toDomainRoles(...))` line. Remove `toDomainRoles` only if unused elsewhere.

Keep `DuplicateUserNameException` propagating (ambiguous username); no new handling.

### 4.2 Tests `JwtExchangeAuthenticationProviderTest.java` (existing)

Add: `subjectHit_updatesIdpDetailsOnly_rolesUntouched`; `subjectMiss_usernameHit_linksSubject_callsUpdateUserAndUpsert`; `subjectMiss_usernameHit_rolesNotModified` (assert `userForUpdate.getRoles()` equals original); `subjectMiss_usernameMiss_jitDisabled_throwsNotOnboarded`; `subjectMiss_usernameMiss_jitEnabled_createsUser`; `usernameClaimAbsent_jitDisabled_throwsNotOnboarded`. Provider built with `AuthProperties.Provider.Builder` setting `jitEnabled` / `usernameClaimKey`.

## Build and verify per slice

```bash
cd core-services/egov-user && mvn -q -B test -Dtest='<TestClass1>,<TestClass2>'
mvn -q -B package -DskipTests
```

JDK note: tests need JDK 17 per repo memory (Mockito cglib fix in #1424); build target stays Java 8.
