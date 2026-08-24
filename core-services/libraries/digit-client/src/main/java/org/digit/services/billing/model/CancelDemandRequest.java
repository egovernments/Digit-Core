package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelDemandRequest {
    @JsonProperty(value="reasonCode")
    private String reasonCode;
    @JsonProperty(value="note")
    private String note;
}