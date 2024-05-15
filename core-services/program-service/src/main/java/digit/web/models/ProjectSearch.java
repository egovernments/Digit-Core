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
 * Project search criteria
 */
@Schema(description = "Project search criteria")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectSearch   {
        @JsonProperty("ids")

                private List<String> ids = null;

        @JsonProperty("tenantId")
          @NotNull

                private String tenantId = null;

        @JsonProperty("programCode")

        @Size(min=2,max=64)         private String programCode = null;

        @JsonProperty("agencyCode")

        @Size(min=2,max=64)         private String agencyCode = null;

        @JsonProperty("projectCode")

        @Size(min=2,max=64)         private String projectCode = null;

        @JsonProperty("name")

        @Size(min=2,max=64)         private String name = null;


        public ProjectSearch addIdsItem(String idsItem) {
            if (this.ids == null) {
            this.ids = new ArrayList<>();
            }
        this.ids.add(idsItem);
        return this;
        }

}
