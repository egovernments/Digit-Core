package org.egov.egovdocumentuploader.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DocumentCategory {

    @JsonProperty("category")
    private String category;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

}