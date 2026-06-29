package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxHead {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="businessServiceCode")
    private String businessServiceCode;
    @JsonProperty(value="code")
    private String code;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="description")
    private String description;
    @JsonProperty(value="order")
    private Integer order;
    @JsonProperty(value="category")
    private String category;
    @JsonProperty(value="isActive")
    private Boolean isActive;
    @JsonProperty(value="effectiveFrom")
    private Long effectiveFrom;
    @JsonProperty(value="effectiveTo")
    private Long effectiveTo;
    @JsonProperty(value="version")
    private Integer version;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}