# egov-user-event

The objective of this service is to create a common point to manage all the events generated for the user in the system. 
Events include updates from multiple applications like PT, PGR, TL etc, events created by the employee addressing the citizen etc. 
This service provides APIs to create , update and search such events for the user.

## 3.0 API (breaking change)

The REST layer implements the 3.0 contract in `user-events-3.0.yml` and **replaces** the legacy DIGIT v1 API. Key changes:

- **No RequestInfo wrapper.** Tenant comes from the mandatory `X-Tenant-ID` header; caller identity from `X-User-ID` (gateway-populated). Bearer auth is enforced at the gateway; the service forwards the raw token to MDMS/localization.
- **Roles come from the bearer JWT** — the service decodes the token payload (without signature verification; the gateway is trusted) and reads `realm_access.roles`, filtering Keycloak plumbing roles (`default-roles-*`, `offline_access`, `uma_authorization`). Dotless roles like `SUPERUSER` are normalized to `SUPERUSER.SUPERUSER` for recipient matching, so role-targeted events (`toRoles`) are deliverable to JWT holders of that role. Without a token, a single role equal to the inferred user type is synthesized.
- **Paths**: `/v1/events/_create` (returns **201**), `/v1/events/_update`, `/v1/events/_search` (query params only, no body), `/notification/_count` and `/lat/_update` (both moved out of the `/v1/events` prefix).
- **Bodies**: create/update take `{"events": [...]}` (1–100 events); responses are bare JSON arrays of events. Errors are bare arrays of `{code, message, description, params}` with 400/404/500 statuses.
- **Status enum** is `ACTIVE | INACTIVE | CANCELED` (single L) on the wire; internally and in the DB it remains `CANCELLED`.
- `X-User-ID` is mandatory for create, update, count, and lat; optional for search.
- Response headers `X-Response-Time`, `X-Response-Timestamp` and echoes of `X-Request-ID`/`X-Correlation-ID`/`X-Tenant-ID` are set by the service. The `X-Rate-Limit*` headers in the spec are left to the gateway.

Behavioral notes and accepted gaps:

- **Search mode**: supplying `userIds` or `roles` query params runs the search in citizen mode ("my notifications" — the service replaces those filters with the caller's own uuid); otherwise it is a tenant-scoped employee search. The legacy `SYSTEM` open-search is unreachable over REST (the Kafka path still supports it).
- **EVENTSONGROUND cannot be created via the 3.0 REST API** — the contract has no `eventCategory`, which MDMS validation mandates (deterministic 400). Creation still works via the Kafka consumer topics; updates of existing EVENTSONGROUND work (internal fields are restored from the DB).
- The internal `name` field is derived from the description (max 65 chars) on create; counter-event localization and the `name` search filter see the derived value.
- The Kafka consumer topics (`persist-user-events-async`, `update-user-events-async`) keep the **legacy** `EventRequest` payload with `RequestInfo`; persistence itself is now direct via JPA (see "Persistence" below).

### DB UML Diagram

- TBD

### Service Dependencies

- egov-mdms-service
- egov-localization

### Swagger API Contract

Link to the swagger API contract yaml and editor link like below

https://github.com/egovernments/DIGIT-Dev/blob/master/municipal-services/docs/user-events.yml


## Service Details

This service manages user events on the egov-platform, which means all the events about which the user (essentially citizen) has to be notified are stored and retrieved through this service. 
Events can be created either by an API call or through pushing records to the Kafka Queue.

**Configurable Properties:**

Following are the properties in application.properties file in egov-user-events service which are configurable.

| Property                     | Value    | Remarks                    | 
| -----------------------------| ---------| ---------------------------|
| `mseva.notif.search.offset`  | 0        | Default pagination offset. |
| `mseva.notif.search.limit`   | 200      | Default pagination limit.  |


### API Details

`/_create` : API to create events in the system.

`/_update` : API to update events in the system.

`/_search` : API to search events in the system.

`/notification/_count` : API to fetch the count of total, unread, read notifications.

`/lat/_update` : API to update the last-login-time of the user. We store last-login-time of the user through this API thereby deciding which notifications have been read.


### Reference Document

All the details and configurations on the services are explained in the document `https://digit-discuss.atlassian.net/l/c/rMA1ukFc`

### Kafka Consumers

`persist-user-events-async` : Topic to which the user-events consumer is subscribed. Producers willing to create events must push records to this topic.
`update-user-events-async` : Topic to which the user-events consumer is subscribed. Producers willing to update events must push records to this topic.

### Persistence

Events, the recipient registry, and last-access times are written **directly by this service via Spring Data JPA** in a single transaction — writes commit before the API responds. The former egov-persister flow (topics `save-user-events`, `update-user-events`, `user-events-lat` and `egov-user-event-persister.yml`) is retired; that persister config is no longer used by this service. Update semantics match the old persister: `tenantid`, `source`, `eventtype`, `postedby`, `referenceid` are immutable on update, and the recipient registry is fully replaced per event.