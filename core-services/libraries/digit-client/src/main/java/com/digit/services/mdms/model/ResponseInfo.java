package com.digit.services.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response info for API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseInfo {
    
    @JsonProperty("apiId")
    private String apiId;
    
    @JsonProperty("ver")
    private String ver;
    
    @JsonProperty("ts")
    private Long ts;
    
    @JsonProperty("resMsgId")
    private String resMsgId;
    
    @JsonProperty("msgId")
    private String msgId;
    
    @JsonProperty("status")
    private String status;
}
