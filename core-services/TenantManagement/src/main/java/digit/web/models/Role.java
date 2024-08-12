package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.AuditDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Details of a role
 */
@Schema(description = "Details of a role")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role   {
        @JsonProperty("id")

          @Valid
                private UUID id = null;

        @JsonProperty("tenantId")
          @NotNull

        @Size(min=1,max=64)         private String tenantId = null;

        @JsonProperty("code")
          @NotNull

        @Size(min=1,max=64)         private String code = null;

        @JsonProperty("actions")

                private List<String> actions = null;

        @JsonProperty("isActive")
          @NotNull

                private Boolean isActive = null;

        @JsonProperty("auditDetails")

          @Valid
                private AuditDetails auditDetails = null;


        public Role addActionsItem(String actionsItem) {
            if (this.actions == null) {
            this.actions = new ArrayList<>();
            }
        this.actions.add(actionsItem);
        return this;
        }

}
