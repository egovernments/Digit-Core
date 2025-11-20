package com.digit.services.workflow.model;

import com.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @JsonProperty("id")
    private String id;

    @JsonProperty("documentType")
    private String documentType;

    @JsonProperty("fileStoreId")
    private String fileStoreId;

    @JsonProperty("documentUid")
    private String documentUid;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
