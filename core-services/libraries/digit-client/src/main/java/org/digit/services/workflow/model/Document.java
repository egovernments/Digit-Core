package org.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @JsonProperty(value="documentType")
    private String documentType;
    @JsonProperty(value="fileStoreId")
    private String fileStoreId;
    @JsonProperty(value="documentUid")
    private String documentUid;
    @JsonProperty(value="additionalDetails")
    private Map<String, Object> additionalDetails;
}