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
public class Document {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="documentType")
    private String documentType;
    @JsonProperty(value="fileStoreId")
    private String fileStoreId;
    @JsonProperty(value="documentUid")
    private String documentUid;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}