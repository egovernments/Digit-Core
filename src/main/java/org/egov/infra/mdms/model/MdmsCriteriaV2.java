package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MdmsCriteria
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsCriteriaV2 {

    @JsonProperty("tenantId")
    @Size(min = 1, max = 100)
    @NotNull
    private String tenantId = null;

    @JsonProperty("ids")
    private Set<String> ids = null;

    @JsonProperty("uniqueIdentifiers")
    @Size(min = 1, max = 64)
    private Set<String> uniqueIdentifiers = null;

    @JsonProperty("schemaCode")
    private String schemaCode = null;

    @JsonProperty("filters")
    private Map<String, String> filterMap = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonIgnore
    private Map<String, String> schemaCodeFilterMap = null;

    @JsonIgnore
    private Set<String> uniqueIdentifiersForRefVerification = null;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;


}
