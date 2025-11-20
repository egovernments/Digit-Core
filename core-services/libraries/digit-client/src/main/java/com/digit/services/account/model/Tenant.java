package com.digit.services.account.model;

import com.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tenant model representing tenant data from Account service.
 * Based on the actual Go service implementation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tenant {

    @JsonProperty("id")
    private String id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("logoId")
    private String logoId;

    @JsonProperty("imageId")
    private String imageId;

    @JsonProperty("domainUrl")
    private String domainUrl;

    @JsonProperty("type")
    private String type;

    @JsonProperty("twitterUrl")
    private String twitterUrl;

    @JsonProperty("facebookUrl")
    private String facebookUrl;

    @JsonProperty("emailId")
    private String emailId;

    @JsonProperty("OfficeTimezone")
    private String officeTimezone;

    @JsonProperty("city")
    private City city;

    @JsonProperty("address")
    private String address;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("contactNumber")
    private String contactNumber;

    @JsonProperty("helpLineNumber")
    private String helpLineNumber;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class City {

        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("localName")
        private String localName;

        @JsonProperty("districtCode")
        private String districtCode;

        @JsonProperty("districtName")
        private String districtName;

        @JsonProperty("districtTenantCode")
        private String districtTenantCode;

        @JsonProperty("regionName")
        private String regionName;

        @JsonProperty("ulbGrade")
        private String ulbGrade;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("shapeFileLocation")
        private String shapeFileLocation;

        @JsonProperty("captcha")
        private String captcha;

        @JsonProperty("code")
        private String code;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;
    }

}
