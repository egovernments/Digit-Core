package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.AdditionalInfo;
import digit.web.models.AuditDetails;
import digit.web.models.Individual;
import digit.web.models.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Exchange message content which will be shared with all request
 */
@Schema(description = "Exchange message content which will be shared with all request")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Disburse   {
        @JsonProperty("id")

          @Valid
                private UUID id = null;

        @JsonProperty("tenantId")
          @NotNull

        @Size(min=2,max=64)         private String tenantId = null;

        @JsonProperty("programCode")
          @NotNull

        @Size(min=2,max=64)         private String programCode = null;

        @JsonProperty("projectCode")
          @NotNull

        @Size(min=2,max=64)         private String projectCode = null;

        @JsonProperty("targetId")
          @NotNull

                private String targetId = null;

        @JsonProperty("transactionId")

        @Size(min=2,max=64)         private String transactionId = null;

        @JsonProperty("sanctionId")

                private String sanctionId = null;

        @JsonProperty("accountCode")
          @NotNull

                private String accountCode = null;

        @JsonProperty("netAmount")
          @NotNull

          @Valid
                private BigDecimal netAmount = null;

        @JsonProperty("grossAmount")
          @NotNull

          @Valid
                private BigDecimal grossAmount = null;

        @JsonProperty("individual")

          @Valid
                private Individual individual = null;

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
