package org.egov.enc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response from POST /crypto/v1/_generatekey.
 *   created=true  → a new key was generated and persisted
 *   created=false → tenant already had a key; this is a no-op
 *
 * The `keyId` is always populated on success (whether newly generated or
 * pre-existing) so callers can correlate downstream encrypt requests.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerateKeyResponse {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("created")
    private boolean created;

    @JsonProperty("keyId")
    private Integer keyId;
}
