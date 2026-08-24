package org.digit.services.billing.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Asks the service to generate bills for every eligible consumer of a business service. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkBillRequest {
    @JsonProperty("businessServiceCode")
    private String businessServiceCode;
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
