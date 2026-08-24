package org.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A list of process definitions.
 *
 * <p>A distinct shape from a single definition: listing wraps the results, whereas fetching one by
 * code returns it bare. {@code page} is always 0 and {@code size} always equals the count — the
 * service does not paginate this route.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowProcessDefinitionListResponse {
    @JsonProperty("totalCount")
    private int totalCount;
    @JsonProperty("page")
    private int page;
    @JsonProperty("size")
    private int size;
    @JsonProperty("definitions")
    private List<WorkflowProcessResponse> definitions;
}
