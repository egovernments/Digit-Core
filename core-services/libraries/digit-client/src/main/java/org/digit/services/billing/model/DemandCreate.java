package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandCreate {
    @JsonProperty(value="businessServiceCode")
    private String businessServiceCode;
    @JsonProperty(value="periodFrom")
    private Long periodFrom;
    @JsonProperty(value="periodTo")
    private Long periodTo;
    @JsonProperty(value="consumerCode")
    private String consumerCode;
    @JsonProperty(value="billExpiryDays")
    private Integer billExpiryDays;
    @JsonProperty(value="payer")
    private List<String> payer;
    @JsonProperty(value="arrearDemandIds")
    private List<String> arrearDemandIds;
    @JsonProperty(value="lineItems")
    private List<LineItemCreate> lineItems;
    @JsonProperty(value="status")
    private DemandStatus status;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}