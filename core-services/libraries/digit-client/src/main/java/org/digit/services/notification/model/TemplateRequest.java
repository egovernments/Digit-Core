package org.digit.services.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A template to create or publish a new version of. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {
    @JsonProperty("templateId")
    private String templateId;
    /** EMAIL or SMS. */
    @JsonProperty("type")
    private String type;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("content")
    private String content;
    @JsonProperty("isHTML")
    private boolean isHTML;
}
