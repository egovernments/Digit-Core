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
public class Demand {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="businessServiceCode")
    private String businessServiceCode;
    @JsonProperty(value="consumerCode")
    private String consumerCode;
    @JsonProperty(value="payer")
    private List<String> payer;
    @JsonProperty(value="periodFrom")
    private Long periodFrom;
    @JsonProperty(value="periodTo")
    private Long periodTo;
    @JsonProperty(value="lineItems")
    private List<LineItem> lineItems;
    @JsonProperty(value="status")
    private DemandStatus status;
    @JsonProperty(value="totalAmount")
    private Double totalAmount;
    @JsonProperty(value="totalCollectedAmount")
    private Double totalCollectedAmount;
    @JsonProperty(value="isDemandPaid")
    private Boolean isDemandPaid;
    @JsonProperty(value="version")
    private Integer version;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;

    public static enum DemandStatus {
        ACTIVE,
        CANCELLED,
        INACTIVE,
        ADJUSTED;

    }
}