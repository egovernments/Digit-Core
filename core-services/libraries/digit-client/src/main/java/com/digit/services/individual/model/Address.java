package com.digit.services.individual.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Address model representing physical address entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    @JsonProperty("id")
    private String id;

    @JsonProperty("clientReferenceId")
    private String clientReferenceId;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("doorNo")
    private String doorNo;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("locationAccuracy")
    private Double locationAccuracy;

    @JsonProperty("type")
    private String type;

    @JsonProperty("addressLine1")
    private String addressLine1;

    @JsonProperty("addressLine2")
    private String addressLine2;

    @JsonProperty("landmark")
    private String landmark;

    @JsonProperty("city")
    private String city;

    @JsonProperty("region")
    private String region;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("buildingName")
    private String buildingName;

    @JsonProperty("street")
    private String street;

    @JsonProperty("country")
    private String country;

    @JsonProperty("formatted")
    private String formatted;

    @JsonProperty("localityCode")
    private String localityCode;

    @JsonProperty("wardCode")
    private String wardCode;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    @JsonProperty("createdTime")
    private Long createdTime;

    @JsonProperty("lastModifiedTime")
    private Long lastModifiedTime;
}
