package org.digit.services.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendSMSRequest {
    @JsonProperty(value="templateId")
    private String templateId;
    @JsonProperty(value="version")
    private String version;
    @JsonProperty(value="mobileNumbers")
    private List<String> mobileNumbers;
    @JsonProperty(value="enrich")
    private boolean enrich;
    @JsonProperty(value="payload")
    private Map<String, Object> payload;
    @JsonProperty(value="category")
    private SMSCategory category;

    public static enum SMSCategory {
        OTP,
        TRANSACTION,
        PROMOTION,
        NOTIFICATION,
        OTHERS;

    }
}