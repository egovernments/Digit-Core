package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.AdditionalInfo;
import digit.web.models.AuditDetails;
import digit.web.models.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * 1. Describes payment schema that enable transfer from payer to payee accounts.  2. This entity supports immediate and scheduling one time payment request into future.  3. Recurring payments is not part of the scope of this entity. 
 */
@Schema(description = "1. Describes payment schema that enable transfer from payer to payee accounts.  2. This entity supports immediate and scheduling one time payment request into future.  3. Recurring payments is not part of the scope of this entity. ")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project   {
        @JsonProperty("id")

          @Valid
                private UUID id = null;

        @JsonProperty("tenantId")
          @NotNull

        @Size(min=2,max=64)         private String tenantId = null;

        @JsonProperty("programCode")
          @NotNull

        @Size(min=2,max=64)         private String programCode = null;

        @JsonProperty("agencyCode")
          @NotNull

        @Size(min=2,max=64)         private String agencyCode = null;

        @JsonProperty("projectCode")

        @Size(min=2,max=64)         private String projectCode = null;

        @JsonProperty("name")
          @NotNull

        @Size(min=2,max=64)         private String name = null;

        @JsonProperty("description")

        @Size(min=2,max=256)         private String description = null;

        @JsonProperty("status")

          @Valid
                private Status status = null;

        @JsonProperty("additionalDetails")

          @Valid
                private AdditionalInfo additionalDetails = null;

        @JsonProperty("auditDetails")

          @Valid
                private AuditDetails auditDetails = null;


}
