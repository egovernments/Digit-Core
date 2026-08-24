package org.digit.services.common.model;

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
public class AuditDetails {
    @JsonProperty(value="createdBy")
    private String createdBy;
    @JsonProperty(value="createdTime")
    private Long createdTime;
    @JsonProperty(value="modifiedBy")
    private String modifiedBy;
    @JsonProperty(value="modifiedTime")
    private Long modifiedTime;

    public String getLastModifiedBy() {
        return this.modifiedBy;
    }

    public Long getLastModifiedTime() {
        return this.modifiedTime;
    }

    public void setLastModifiedBy(String v) {
        this.modifiedBy = v;
    }

    public void setLastModifiedTime(Long v) {
        this.modifiedTime = v;
    }
}