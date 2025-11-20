package com.digit.services.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendSMSRequest {

    @JsonProperty("templateId")
    private String templateId;

    @JsonProperty("version")
    private String version;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("mobileNumbers")
    private List<String> mobileNumbers;

    @JsonProperty("enrich")
    private boolean enrich;

    @JsonProperty("payload")
    private Map<String, Object> payload;

    @JsonProperty("category")
    private SMSCategory category;

    // Enum representing SMS categories
    public enum SMSCategory {
        OTP,
        TRANSACTION,
        PROMOTION,
        NOTIFICATION,
        OTHERS
    }
}
