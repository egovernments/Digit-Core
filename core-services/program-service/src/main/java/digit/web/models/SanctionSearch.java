package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * Sanction search criteria
 */
@Schema(description = "Sanction search criteria")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SanctionSearch   {
        @JsonProperty("ids")

                private List<String> ids = null;

        @JsonProperty("tenantId")
          @NotNull

                private String tenantId = null;

        @JsonProperty("programCode")

                private String programCode = null;

        @JsonProperty("projectCode")

                private String projectCode = null;


        public SanctionSearch addIdsItem(String idsItem) {
            if (this.ids == null) {
            this.ids = new ArrayList<>();
            }
        this.ids.add(idsItem);
        return this;
        }

}
