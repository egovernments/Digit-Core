package org.egov.user.domain.model.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;


@Validated


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {
    @JsonProperty("id")
    @Size(min = 2, max = 64)
    private String id = null;

    @JsonIgnore
    private String projectid = null;

    @JsonProperty("tenantId")
    @NotNull
    @Size(min = 2, max = 128)
    private String tenantId = null;

    @JsonProperty("documentType")
    @NotNull
    @Size(min = 2, max = 64)
    private String documentType = null;

    @JsonProperty("fileStoreId")
    @NotNull
    @Size(min = 2, max = 64)
    private String fileStoreId = null;

    @JsonProperty("documentUid")
    @Size(min = 2, max = 64)
    private String documentUid = null;

    @JsonProperty("fileStore")
    private String fileStore = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

}

