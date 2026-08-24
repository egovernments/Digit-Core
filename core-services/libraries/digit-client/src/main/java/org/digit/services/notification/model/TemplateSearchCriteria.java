package org.digit.services.notification.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for a template search.
 *
 * <p>{@code ids} travels as a single comma-separated parameter, which is how the service reads it.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSearchCriteria {
    @JsonProperty("ids")
    private List<String> ids;
    @JsonProperty("templateId")
    private String templateId;
    @JsonProperty("version")
    private String version;
    @JsonProperty("type")
    private String type;
    @JsonProperty("isHTML")
    private Boolean isHTML;
    @JsonProperty("limit")
    private Integer limit;
    @JsonProperty("offset")
    private Integer offset;
}
