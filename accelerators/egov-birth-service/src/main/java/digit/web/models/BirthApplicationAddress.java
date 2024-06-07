package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import org.egov.common.contract.models.Address;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * BirthApplicationAddress
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-13T14:58:44.100316977+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BirthApplicationAddress   {
        @JsonProperty("id")

          @Valid
                private String id = null;

        @JsonProperty("tenantId")
          @NotNull

        @Size(min=2,max=64)         private String tenantId = null;

        @JsonProperty("applicationNumber")

                private String applicationNumber = null;

        @JsonProperty("applicantAddress")

          @Valid
                private Address applicantAddress = null;


}
