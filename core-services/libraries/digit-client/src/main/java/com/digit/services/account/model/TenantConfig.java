package com.digit.services.account.model;

import com.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * TenantConfig model representing tenant configuration data from Account service.
 * Based on the actual Go service implementation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfig {

    @JsonProperty("id")
    private String id;

    @JsonProperty("defaultLoginType")
    private String defaultLoginType;

    @JsonProperty("otpLength")
    private String otpLength;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("enableUserBasedLogin")
    private Boolean enableUserBasedLogin;

    @JsonProperty("additionalAttributes")
    private Map<String, Object> additionalAttributes;

    @JsonProperty("documents")
    private List<Document> documents;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("languages")
    private List<String> languages;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    // Explicit getter/setter methods as fallback for Lombok
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public AuditDetails getAuditDetails() {
        return auditDetails;
    }

    public void setAuditDetails(AuditDetails auditDetails) {
        this.auditDetails = auditDetails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {

        @JsonProperty("id")
        private String id;

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("tenantConfigId")
        private String tenantConfigId;

        @JsonProperty("type")
        private String type;

        @JsonProperty("fileStoreId")
        private String fileStoreId;

        @JsonProperty("url")
        private String url;

        @JsonProperty("isActive")
        private Boolean isActive;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;
    }

}
