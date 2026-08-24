package org.digit.services.individual.model;

import org.digit.services.common.model.AuditDetails;
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
public class Address {
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("doorNo")
    private String doorNo;
    @JsonProperty("buildingName")
    private String buildingName;
    @JsonProperty("street")
    private String street;
    @JsonProperty("landmark")
    private String landmark;
    @JsonProperty("addressLine1")
    private String addressLine1;
    @JsonProperty("addressLine2")
    private String addressLine2;
    @JsonProperty("city")
    private String city;
    @JsonProperty("region")
    private String region;
    @JsonProperty("country")
    private String country;
    @JsonProperty("pincode")
    private String pincode;
    @JsonProperty("boundaryCode")
    private String boundaryCode;
    @JsonProperty("latitude")
    private Double latitude;
    @JsonProperty("longitude")
    private Double longitude;
    @JsonProperty("locationAccuracy")
    private Double locationAccuracy;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
