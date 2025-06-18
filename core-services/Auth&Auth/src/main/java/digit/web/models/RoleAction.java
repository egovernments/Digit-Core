package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Details of a roleAction
 */
@Schema(description = "Details of a roleAction")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-07-19T14:07:58.669830457+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleAction   {
        @JsonProperty("tenantId")
          @NotNull

        @Size(min=1,max=50)         private String tenantId = null;

        @JsonProperty("roleCode")
          @NotNull

        @Size(min=1,max=20)         private String roleCode = null;

        @JsonProperty("actionId")
          @NotNull

        @Size(min=1,max=20)         private String actionId = null;


}
