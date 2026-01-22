package org.egov.user.domain.model.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

/**
* Target
*/
@Validated


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Target {
    @JsonProperty("id")
    private String id = null;

    @JsonIgnore
    private String projectid = null;

    @JsonProperty("beneficiaryType")
    private BeneficiaryType beneficiaryType = null;

    @JsonProperty("totalNo")
    private Integer totalNo = null;

    @JsonProperty("targetNo")
    private Integer targetNo = null;

    @JsonProperty("isDeleted")
    private Boolean isDeleted = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;


}

