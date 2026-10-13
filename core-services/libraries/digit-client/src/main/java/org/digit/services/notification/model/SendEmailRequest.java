package org.digit.services.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
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
public class SendEmailRequest {
    @JsonProperty(value="templateId")
    private String templateId;
    @JsonProperty(value="version")
    private String version;
    @JsonProperty(value="emailIds")
    private List<String> emailIds;
    @JsonProperty(value="enrich")
    private boolean enrich;
    @JsonProperty(value="payload")
    private Map<String, Object> payload;
    @JsonProperty(value="attachments")
    private List<String> attachments;
}