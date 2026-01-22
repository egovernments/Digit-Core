package org.egov.user.domain.model.project;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;


@Validated


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("projectNumber")
    private String projectNumber = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("projectType")
    private String projectType = null;

    @JsonProperty("projectSubType")
    private String projectSubType = null;

    @JsonProperty("department")
    private String department = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("referenceID")
    private String referenceID = null;

    @JsonProperty("projectTypeId")
    private String projectTypeId = null;

    @JsonProperty("documents")
    @Valid
    private List<Document> documents = null;

    @JsonProperty("address")
    private Address address = null;

    @JsonProperty("startDate")
    private Long startDate = null;

    @JsonProperty("endDate")
    private Long endDate = null;

    @JsonProperty("isTaskEnabled")
    private Boolean isTaskEnabled = false;

    @JsonProperty("parent")
    private String parent = null;

    @JsonProperty("projectHierarchy")
    private String projectHierarchy = null;

    @JsonProperty("natureOfWork")
    private String natureOfWork = null;

    @JsonProperty("ancestors")
    private List<Project> ancestors = null;

    @JsonProperty("descendants")
    private List<Project> descendants = null;

    @JsonProperty("targets")
    @Valid
    private List<Target> targets = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;

    @JsonProperty("rowVersion")
    private Integer rowVersion = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;


    public Project addDocumentsItem(Document documentsItem) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        this.documents.add(documentsItem);
        return this;
    }

    public Project addTargetsItem(Target targetsItem) {
        if (this.targets == null) {
            this.targets = new ArrayList<>();
        }
        this.targets.add(targetsItem);
        return this;
    }

    public Project addDescendant(Project project) {
        if (this.descendants == null) {
            this.descendants = new ArrayList<>();
        }
        this.descendants.add(project);
        return this;
    }

}

