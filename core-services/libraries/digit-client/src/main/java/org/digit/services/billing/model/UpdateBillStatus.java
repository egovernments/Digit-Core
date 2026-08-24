package org.digit.services.billing.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cancels bills for a consumer by moving them to another status. All four fields are required. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBillStatus {
    @JsonProperty("businessServiceCode")
    private String businessServiceCode;
    @JsonProperty("consumerCode")
    private String consumerCode;
    @JsonProperty("statusToBeUpdated")
    private BillStatus statusToBeUpdated;
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
