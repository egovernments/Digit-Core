package org.digit.services.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The rendered result of a preview. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePreviewResponse {
    @JsonProperty("templateId")
    private String templateId;
    @JsonProperty("version")
    private String version;
    @JsonProperty("type")
    private String type;
    @JsonProperty("isHTML")
    private boolean isHTML;
    @JsonProperty("renderedSubject")
    private String renderedSubject;
    @JsonProperty("renderedContent")
    private String renderedContent;
}
