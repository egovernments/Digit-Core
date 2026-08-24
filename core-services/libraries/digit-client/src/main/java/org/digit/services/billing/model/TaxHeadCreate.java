package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A tax head to register — the line-item type a demand's amounts are attributed to.
 *
 * <p>Required: {@code code}, {@code name}, {@code businessServiceCode}, {@code order},
 * {@code effectiveFrom} and {@code isActive}. {@code category} defaults to {@code OTHER}.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxHeadCreate {
    /** Upper-case, 2..64 characters — a wider allowance than a business service code. */
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    /** Must match an existing business service. */
    @JsonProperty("businessServiceCode")
    private String businessServiceCode;
    @JsonProperty("category")
    private TaxHeadCategory category;
    /** Position within the business service's heads; at least 1. */
    @JsonProperty("order")
    private Integer order;
    /** Epoch millis. */
    @JsonProperty("effectiveFrom")
    private Long effectiveFrom;
    @JsonProperty("effectiveTo")
    private Long effectiveTo;
    @JsonProperty("isActive")
    private Boolean isActive;
}
