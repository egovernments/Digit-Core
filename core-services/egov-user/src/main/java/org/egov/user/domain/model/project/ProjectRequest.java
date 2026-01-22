package org.egov.user.domain.model.project;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

/**
* ProjectRequest
*/
@Validated


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectRequest   {
    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("Projects")
    @NotNull
    @Valid
    @Size(min=1)
    private List<Project> projects = new ArrayList<>();

    @JsonProperty("isCascadingProjectDateUpdate")
    @Valid
    private boolean isCascadingProjectDateUpdate = false;

    @JsonProperty("apiOperation")
    @Valid
    private ApiOperation apiOperation = null;

    public ProjectRequest addProjectItem(Project projectItem) {
        this.projects.add(projectItem);
        return this;
    }

}

