package org.digit.services.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request context for the canonical (envelope) APIs, where identity travels in the body instead of
 * headers.
 *
 * <p>Useful when there is no inbound request to take headers from — notably tenant onboarding, which
 * acts as a newly created tenant's own user. The header-based routes of some services require both a
 * tenant and a user header on every call, including reads, whereas the canonical routes are exempt
 * from that check.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"apiId", "ver", "ts", "action", "msgId", "requestId", "correlationId",
        "authToken", "tenantId", "userInfo"})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {
    @JsonProperty("apiId")
    private String apiId;
    @JsonProperty("ver")
    private String ver;
    /** Epoch millis. Some services require 13 digits. */
    @JsonProperty("ts")
    private Long ts;
    @JsonProperty("action")
    private String action;
    @JsonProperty("msgId")
    private String msgId;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("correlationId")
    private String correlationId;
    @JsonProperty("authToken")
    private String authToken;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("userInfo")
    private UserInfo userInfo;
}
