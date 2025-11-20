package com.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {

    @JsonProperty("action")
    private String action;

    @JsonProperty("assignes")
    private List<String> assignes;

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("verificationDocuments")
    private List<Document> verificationDocuments;
}
