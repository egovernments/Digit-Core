package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Workflow
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-12T13:40:42.981935029+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Workflow   {
        @JsonProperty("action")
          @NotNull

                private String action = null;

        @JsonProperty("comment")

                private String comment = null;

        @JsonProperty("assignees")

                private List<String> assignees = null;

        @JsonProperty("status")

                private String status = null;

        @JsonProperty("documents")
          @Valid
                private List<Document> documents = null;


        public Workflow addAssigneesItem(String assigneesItem) {
            if (this.assignees == null) {
            this.assignees = new ArrayList<>();
            }
        this.assignees.add(assigneesItem);
        return this;
        }

        public Workflow addDocumentsItem(Document documentsItem) {
            if (this.documents == null) {
            this.documents = new ArrayList<>();
            }
        this.documents.add(documentsItem);
        return this;
        }

}
