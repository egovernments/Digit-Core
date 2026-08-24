package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandPatch {
    @JsonProperty(value="consumerCode")
    private String consumerCode;
    @JsonProperty(value="payer")
    private List<String> payer;
    @JsonProperty(value="lineItems")
    private List<LineItemCreate> lineItems;
    @JsonProperty(value="status")
    private DemandStatus status;
}