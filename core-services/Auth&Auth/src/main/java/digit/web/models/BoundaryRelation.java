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
 * BoundaryRelation
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-14T17:06:34.078752728+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryRelation   {
        @JsonProperty("id")

                private String id = null;

        @JsonProperty("code")
          @NotNull

                private String code = null;

        @JsonProperty("tenantId")

                private String tenantId = null;

        @JsonProperty("hierarchyType")

                private String hierarchyType = null;

        @JsonProperty("boundaryType")

                private String boundaryType = null;

        @JsonProperty("parent")

                private String parent = null;


}
