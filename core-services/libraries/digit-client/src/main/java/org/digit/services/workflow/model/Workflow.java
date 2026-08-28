package org.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {
    @JsonProperty(value="action")
    private String action;
    @JsonProperty(value="assignes")
    private List<String> assignes;
    @JsonProperty(value="comments")
    private String comments;
    @JsonProperty(value="verificationDocuments")
    private List<Document> verificationDocuments;
}