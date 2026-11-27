package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemaDefHistoryCriteria {

    @JsonProperty("tenantId")
    @Size(min = 1, max = 100)
    @NotNull
    private String tenantId = null;

    @JsonProperty("codes")
    @Size(min = 1, max = 64)
    @NotNull
    private List<String> codes = null;

    @JsonProperty("fromTime")
    private Long fromTime = null;

    @JsonProperty("toTime")
    private Long toTime = null;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;
}
