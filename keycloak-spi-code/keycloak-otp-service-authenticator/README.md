# Keycloak OTP Service Authenticator

A Keycloak [Authenticator SPI](https://www.keycloak.org/docs/latest/server_development/#_auth_spi)
that adds a one-time-password step to a login flow. It does **not** generate or
deliver codes itself — it delegates to an external **OTP microservice** (v3) over
HTTP. Keycloak owns the user, the session, and the login UI; the OTP service owns
code generation, delivery (SMS/email), expiry, rate-limiting, and verification.

It registers two authenticators that share one implementation:

| Display name | Provider ID | Destination |
|---|---|---|
| **OTP – Email** | `otp-email-authenticator` | user's email |
| **OTP – SMS** | `otp-sms-authenticator` | user's `mobileNumber` attribute |

---

## What it does

During a login flow, after the user is identified, the authenticator:

1. **Generates** an OTP — sends the user's destination (`identifier`) + `purpose`
   to the OTP service, which delivers the code out-of-band and returns a
   `referenceId`.
2. **Prompts** the user for the code (browser form) or signals the client to retry
   with the code (direct grant).
3. **Verifies** the entered code against the `referenceId`, and on success lets the
   login proceed.

It supports **resend** and **cancel**, and works in both the **browser flow** and
the **direct-grant (ROPC) flow**.

---

## How it works

### Architecture

```
EmailOtpAuthenticatorFactory ─┐
                              ├─► OtpAuthenticator ──► OtpClient ──HTTP──► OTP service (v3)
SmsOtpAuthenticatorFactory  ──┘     (channel-agnostic)   (JDK HttpClient)
```

- **`OtpAuthenticator`** is channel-agnostic — it contains all the flow logic and
  knows nothing about email vs SMS.
- The two **factories** inject the only things that differ per channel: which user
  attribute holds the destination, and the OTP `purpose`.
- Everything (`OtpConfig`, the HTTP client, `OtpClient`, the authenticator) is built
  **once at server startup** and reused. The authenticator is **stateless and
  thread-safe** — all per-login state lives in the Keycloak auth session, so a single
  shared instance safely serves concurrent logins.

### Service contract (v3)

All calls are `POST {OTP_HOST}{path}` with headers `X-Tenant-Id: <realm name>` and a
generated `X-Request-Id`. Errors come back as a JSON array `[{ "code", "message", … }]`.

| Operation | Endpoint (default) | Request | Response |
|---|---|---|---|
| Generate | `/otp/v3/generate` | `{identifier, purpose}` | `{referenceId, expiresIn, cooldownSeconds}` |
| Resend | `/otp/v3/resend` | `{referenceId}` | `{referenceId, expiresIn, cooldownSeconds, purpose}` |
| Verify | `/otp/v3/verify` | `{referenceId, purpose, otp}` | `{verified, purpose}` |
| Invalidate | `/otp/v3/invalidate` | `{referenceId}` | `{invalidated, purpose}` |

The `identifier` is the raw email/phone; the service infers the type. `verify` requires
the **same `purpose`** the code was generated with.

### Verify outcome mapping

Verification success is HTTP `200` with `verified=true`. Failures arrive as HTTP
statuses and are mapped to Keycloak flow outcomes:

| HTTP | Meaning | Behaviour |
|---|---|---|
| `200` + `verified` | OK | login proceeds |
| `410` / `423` | expired / locked | terminal failure, session cleared |
| `400` / `404` / `422` | bad/unknown/wrong code | retryable — session kept so user can re-enter or resend |
| other | service error | internal error |

### Browser flow

`generate → render OTP form → verify`. The `referenceId` is held in the Keycloak auth
session (the login cookie provides continuity); the user never sees it. The form
supports **resend** and **cancel** (which invalidates the OTP and resets the flow).

```mermaid
sequenceDiagram
    actor U as User (browser)
    participant KC as Keycloak (OtpAuthenticator)
    participant S as OTP Service

    Note over KC: authenticate()
    KC->>S: POST /generate {identifier, purpose}
    S-->>KC: 200 {referenceId}
    KC->>KC: store referenceId in auth session
    S-->>U: deliver code (SMS / email)
    KC-->>U: render OTP form

    U->>KC: submit otp
    KC->>S: POST /verify {referenceId, otp, purpose}
    alt verified (200)
        S-->>KC: {verified: true}
        KC-->>U: login success
    else expired / locked (410 / 423)
        S-->>KC: error
        KC-->>U: fail — code expired, session cleared
    else wrong / unknown code (400 / 404 / 422)
        S-->>KC: error
        KC-->>U: re-show form (retry or resend)
    end

    Note over U,KC: resend → POST /resend · cancel → POST /invalidate + reset flow
```

### Direct grant (ROPC, `grant_type=password`) — two round-trips

There's no session continuity between token calls, so the client carries the
`referenceId`:

- **Round 1** (no `otp`): generate an OTP, respond `400 {"error":"otp_required","referenceId":"…"}`.
- **Round 2** (`otp` + `referenceId`): verify → `200` token, or a JSON error
  (`invalid_otp` / `otp_expired` / `server_error`).

```mermaid
sequenceDiagram
    participant C as Client
    participant KC as Keycloak (OtpAuthenticator)
    participant S as OTP Service

    Note over C,KC: Round 1 — no otp
    C->>KC: POST /token (username, password)
    KC->>S: POST /generate {identifier, purpose}
    S-->>KC: 200 {referenceId}
    S-->>C: deliver code (SMS / email)
    KC-->>C: 400 {error: "otp_required", referenceId}

    Note over C,KC: Round 2 — with otp + referenceId
    C->>KC: POST /token (username, password, otp, referenceId)
    KC->>S: POST /verify {referenceId, otp, purpose}
    alt verified (200)
        S-->>KC: {verified: true}
        KC-->>C: 200 access + refresh token
    else failure (410 / 423 / 4xx)
        S-->>KC: error
        KC-->>C: 400 invalid_otp / otp_expired
    end
```

---

## Configuration

All configuration is via **environment variables**, read once at startup. No Keycloak
Admin-UI config — the factory already knows its channel.

| Variable | Default | Description |
|---|---|---|
| `OTP_HOST` | `http://localhost:8081` | OTP service base URL |
| `OTP_GENERATE_PATH` | `/otp/v3/generate` | generate endpoint path |
| `OTP_RESEND_PATH` | `/otp/v3/resend` | resend endpoint path |
| `OTP_VERIFY_PATH` | `/otp/v3/verify` | verify endpoint path |
| `OTP_INVALIDATE_PATH` | `/otp/v3/invalidate` | invalidate endpoint path |
| `OTP_EMAIL_PURPOSE` | `login` | purpose sent for the email channel |
| `OTP_SMS_PURPOSE` | `login` | purpose sent for the SMS channel |
| `KEYCLOAK_EMAIL_DESTINATION_ATTRIBUTE` | `email` | user attribute holding the email |
| `KEYCLOAK_SMS_DESTINATION_ATTRIBUTE` | `mobileNumber` | user attribute holding the phone |
| `HTTP_CLIENT_CONNECT_TIMEOUT_MS` | `3000` | TCP connect timeout |
| `HTTP_CLIENT_REQUEST_TIMEOUT_MS` | `5000` | per-request response timeout |

The tenant sent to the OTP service (`X-Tenant-Id`) is the Keycloak **realm name** — the
OTP service must have a config for that tenant + purpose.

---

## Build

Requires **JDK 21** and Maven. Targets **Keycloak 25.0.6**.

```bash
mvn clean install
```

Produces a small (~40 KB) jar. HTTP transport uses the **JDK `java.net.http.HttpClient`**
(no Apache dependency); Jackson is `provided` (uses Keycloak's, version-pinned to match).

## Deploy

Copy the jar into Keycloak's providers directory and rebuild:

```bash
cp target/keycloak-otp-service-authenticator-1.0-SNAPSHOT.jar $KEYCLOAK_HOME/providers/
$KEYCLOAK_HOME/bin/kc.sh build
```

Then add **OTP – Email** or **OTP – SMS** as an execution in your authentication flow
(Keycloak Admin → Authentication → your flow → Add step).

---

## Project layout

```
auth/
├── OtpAuthenticator.java              # channel-agnostic flow logic
├── OtpAuthenticatorFactory.java       # abstract base: builds config + client once
├── EmailOtpAuthenticatorFactory.java  # injects email channel
├── SmsOtpAuthenticatorFactory.java    # injects SMS channel
├── config/
│   ├── OtpConfig.java                 # env-driven config
│   └── OtpConstants.java              # session-note / form-param keys
└── clients/otp/
    ├── OtpClient.java                 # client contract
    ├── OtpClientImpl.java             # JDK HttpClient implementation
    ├── OtpClientException.java        # carries HTTP status + parsed error
    └── models/                        # request/response DTOs (v3 contract)
```

---

## Mobile self-registration flow (passwordless)

Two additional authenticators implement an OTP-only **self-registration** flow:
the user enters a mobile number, receives an OTP, and on verification a
**passwordless** account is created (username = mobile number, `mobileNumber`
attribute set, `enabled=true`, `emailVerified=false`, **no password credential**).
Realm default roles/groups apply automatically.

| Display name | Provider ID | Purpose |
|---|---|---|
| **Registration – Mobile Number** | `registration-mobile-number` | Step A: mobile form → uniqueness check → send OTP |
| **Registration – Mobile OTP Verify** | `registration-mobile-otp-verify` | Step B: verify OTP → create user |

Both reuse the same `OtpClient` / `OtpConfig` as the login flow — there is no
second OTP integration. The OTP `purpose` sent to the service is configured via:

| Env var | Default |
|---|---|
| `OTP_REGISTRATION_PURPOSE` | `registration` |

All other settings (`OTP_HOST`, paths, timeouts, `KEYCLOAK_SMS_DESTINATION_ATTRIBUTE`)
are shared with the login authenticators. Resend cooldown, expiry, and rate
limiting are enforced by the OTP service, exactly as in the login flow.

Templates shipped in the jar: `register-mobile.ftl`, `register-mobile-otp.ftl`
(rendered through the active login theme's `template.ftl` — no theme changes needed).

> **Mobile format:** validation is currently a permissive sanity check
> (`^\+?\d{7,15}$`, spaces/dashes stripped). Country-code normalization is
> intentionally deferred — see `MobileRegistrationAuthenticator.MOBILE_PATTERN`.

### Build & deploy

```bash
mvn clean package
cp target/keycloak-otp-service-authenticator-1.0-SNAPSHOT.jar $KEYCLOAK_HOME/providers/
$KEYCLOAK_HOME/bin/kc.sh build     # not needed with start-dev (auto-builds on start)
# restart Keycloak
```

### Flow wiring (Admin Console, Keycloak 25)

1. **Authentication → Flows** → row `registration` → ⋮ → **Duplicate** → name it
   `registration mobile otp`.
2. In the copy, **delete** (or set to *Disabled*) the `registration mobile otp
   registration form` step (the `registration-page-form` sub-flow containing
   *User Profile Creation* / *Password Validation*).
3. **Add step** → **Registration – Mobile Number** → set to **Required**.
4. **Add step** → **Registration – Mobile OTP Verify** → set to **Required**
   (must be *after* Step A, at the same level).
5. ⋮ on the flow → **Bind flow** → **Registration flow**.
6. **Realm settings → Login** → ensure **User registration** is ON (this shows
   the "Register" link on the login page).

### Realm prerequisites (verify these — the flow can't override them)

- **Realm settings → Login**: **"Email as username" must be OFF** — the username
  is the mobile number.
- **Realm settings → User profile**: the default profile marks `email` as
  *required for user*. User creation here bypasses profile validation, but any
  later profile update (account console / admin edit) will fail for email-less
  users — set `email` to not-required (or required for admin only).
- **Authentication → Required actions**: neither **Update Password** nor
  **Verify Email** may be marked *Default action* — either would trap these
  passwordless, email-less accounts right after registration.
- **Browser flow**: for these users to log in afterwards, the browser flow must
  reach **OTP – SMS** without requiring a password (e.g. Username Form →
  OTP – SMS). A *Required* Password step makes these accounts unable to log in.

### Behaviour details

- **Duplicate check** matches both username and the `mobileNumber` attribute;
  a concurrent registration between the two steps is caught via
  `ModelDuplicateException` and surfaced as "already registered".
- **Resend** relays to `/otp/v3/resend`; a 429 (cooldown) keeps the user on the
  form with a message. After a terminal expiry (410/423) the stored
  `referenceId` is dropped and **Resend Code** generates a fresh OTP.
- **Change Number** (cancel) invalidates the outstanding OTP and restarts the
  flow at the mobile-number step.
