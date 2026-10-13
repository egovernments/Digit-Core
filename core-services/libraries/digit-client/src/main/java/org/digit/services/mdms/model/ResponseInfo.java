package org.digit.services.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseInfo {
    @JsonProperty(value="apiId")
    private String apiId;
    @JsonProperty(value="ver")
    private String ver;
    @JsonProperty(value="ts")
    private Long ts;
    @JsonProperty(value="resMsgId")
    private String resMsgId;
    @JsonProperty(value="msgId")
    private String msgId;
    @JsonProperty(value="status")
    private String status;
}