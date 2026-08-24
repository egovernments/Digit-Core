package org.digit.services.workflow.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Aggregate counts across workflow instances, grouped by state or by pending action. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionCountResponse {
    @JsonProperty("countType")
    private String countType;
    @JsonProperty("totalCount")
    private long totalCount;
    @JsonProperty("statusCounts")
    private List<TransitionStatusCount> statusCounts;
    @JsonProperty("actionCounts")
    private List<TransitionActionCount> actionCounts;
}
