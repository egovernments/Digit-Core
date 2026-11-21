package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandUpdate {
    @JsonProperty(value="id")
    private String id;
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
    @JsonProperty(value="arrearSourceDemandIds")
    private List<String> arrearSourceDemandIds;
    @JsonProperty(value="lineItems")
    private List<LineItemCreate> lineItems;
    @JsonProperty(value="status")
    private String status;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}