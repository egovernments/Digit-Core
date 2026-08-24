package org.digit.services.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailResponse {
    @JsonProperty(value="templateId")
    private String templateId;
    @JsonProperty(value="version")
    private String version;
    @JsonProperty(value="status")
    private String status;
}