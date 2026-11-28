package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLog {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("userUUID")
    private String userUUID = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("objectId")
    private String objectId = null;

    @JsonProperty("changeDate")
    private Long changeDate = null;

    @JsonProperty("operationType")
    private String operationType = null;

    @JsonProperty("keyValueMap")
    private Map<String, Object> keyValueMap = null;

    @JsonProperty("integrityHash")
    private String integrityHash = null;

}
