package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A full replacement of a tax head; the code comes from the path. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxHeadUpdate {
    @JsonProperty("name")
    private String name;
    @JsonProperty("businessServiceCode")
    private String businessServiceCode;
    @JsonProperty("category")
    private TaxHeadCategory category;
    @JsonProperty("order")
    private Integer order;
    @JsonProperty("effectiveFrom")
    private Long effectiveFrom;
    @JsonProperty("effectiveTo")
    private Long effectiveTo;
    @JsonProperty("isActive")
    private Boolean isActive;
}
