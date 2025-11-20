package com.digit.services.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendEmailRequest {

    @JsonProperty("templateId")
    private String templateId;

    @JsonProperty("version")
    private String version;

    @JsonProperty("emailIds")
    @Builder.Default
    private List<String> emailIds = new ArrayList<>();

    @JsonProperty("enrich")
    private boolean enrich;

    @JsonProperty("payload")
    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();

    @JsonProperty("attachments")
    @Builder.Default
    private List<String> attachments = new ArrayList<>();
}
