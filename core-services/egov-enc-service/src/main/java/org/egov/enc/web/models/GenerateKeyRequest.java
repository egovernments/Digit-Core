package org.egov.enc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /crypto/v1/_generatekey.
 *
 * Generates a symmetric + asymmetric key pair for the given tenantId if one
 * doesn't already exist. Idempotent — re-issuing for an existing tenant
 * returns the current keyId without rotating.
 *
 * Use case: callers that provision new tenants (e.g. MCP tenant_bootstrap)
 * need a key to exist BEFORE the first encrypt request for that tenant.
 * The default key-generation path (init() + checkIfTenantExists) only fires
 * for tenants reachable via MDMS search under STATE_LEVEL_TENANT_ID, which
 * excludes brand-new state roots.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerateKeyRequest {

    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;
}
