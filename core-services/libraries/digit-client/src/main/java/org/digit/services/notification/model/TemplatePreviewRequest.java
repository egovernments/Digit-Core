package org.digit.services.notification.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Renders a template against a payload without sending anything. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePreviewRequest {
    @JsonProperty("templateId")
    private String templateId;
    @JsonProperty("version")
    private String version;
    @JsonProperty("enrich")
    private boolean enrich;
    @JsonProperty("payload")
    private Map<String, Object> payload;
}
