package org.digit.services.billing.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Acknowledges a bulk bill run.
 *
 * <p>The work is queued rather than done — the service answers 202 — so this reports what was
 * scheduled, not what was produced. Track progress by the {@code requestId}.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkBillResponse {
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("businessServiceCode")
    private String businessServiceCode;
    @JsonProperty("status")
    private BulkBillStatus status;
    @JsonProperty("totalJobsCreated")
    private int totalJobsCreated;
    @JsonProperty("totalConsumersIdentified")
    private int totalConsumersIdentified;
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
