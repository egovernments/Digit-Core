package org.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for a workflow instance search.
 *
 * <p>The service accepts at most <em>one</em> of {@code entityId}, {@code currentState},
 * {@code assignee} and {@code escalated} per call, and treats none of them as inbox mode — the
 * instances awaiting the calling user.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTransitionSearchCriteria {
    @JsonProperty("entityId")
    private String entityId;
    @JsonProperty("processCode")
    private String processCode;
    @JsonProperty("version")
    private String version;
    @JsonProperty("currentState")
    private String currentState;
    @JsonProperty("assignee")
    private String assignee;
    @JsonProperty("escalated")
    private Boolean escalated;
    @JsonProperty("history")
    private Boolean history;
    @JsonProperty("page")
    private Integer page;
    @JsonProperty("size")
    private Integer size;
}
