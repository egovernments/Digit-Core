package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateBillCriteria {
    @JsonProperty(value="businessServiceCode")
    private String businessServiceCode;
    @JsonProperty(value="consumerCode")
    private String consumerCode;
    @JsonProperty(value="payerId")
    private String payerId;
    @JsonProperty(value="payerName")
    private String payerName;
    @JsonProperty(value="payerAddress")
    private String payerAddress;
    @JsonProperty(value="payerMobileNumber")
    private String payerMobileNumber;
    @JsonProperty(value="payerEmail")
    private String payerEmail;
}