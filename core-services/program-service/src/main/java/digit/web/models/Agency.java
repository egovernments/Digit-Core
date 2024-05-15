package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.AuditDetails;
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
 * Describes an agency
 */
@Schema(description = "Describes an agency")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Agency   {
        @JsonProperty("id")

          @Valid
                private UUID id = null;

        @JsonProperty("tenantId")
          @NotNull

                private String tenantId = null;

        @JsonProperty("agencyCode")

        @Size(min=2,max=64)         private String agencyCode = null;

        @JsonProperty("agencyType")
          @NotNull

                private String agencyType = null;

        @JsonProperty("programCode")
          @NotNull

        @Size(min=2,max=64)         private String programCode = null;

        @JsonProperty("orgNumber")
          @NotNull

        @Size(min=1,max=64)         private String orgNumber = null;

        @JsonProperty("auditDetails")

          @Valid
                private AuditDetails auditDetails = null;


}
