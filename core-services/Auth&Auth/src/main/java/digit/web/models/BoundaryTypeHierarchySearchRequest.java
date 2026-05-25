package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import digit.web.models.RequestInfo1;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * BoundaryTypeHierarchySearchRequest
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-14T17:06:34.078752728+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryTypeHierarchySearchRequest   {
        @JsonProperty("RequestInfo")

          @Valid
                private RequestInfo1 requestInfo = null;

        @JsonProperty("BoundaryTypeHierarchySearchCriteria")

          @Valid
                private BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria = null;


}
