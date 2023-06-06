package org.egov.common.contract.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Workflow {
    @JsonProperty("action")
    private String action = null;

    @JsonProperty("comments")
    private String comments = null;

    @JsonProperty("documents")
    @Valid
    private List<Document> documents = null;

    @JsonProperty("assignes")
    @Valid
    private List<String> assignes = null;

    @JsonProperty("rating")
    private Integer rating = null;

    public Workflow addDocumentsItem(Document documentsItem) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        this.documents.add(documentsItem);
        return this;
    }

}
