package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.BoundaryTypeHierarchy;
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
 * BoundaryTypeHierarchyDefinition
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-14T17:06:34.078752728+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryTypeHierarchyDefinition   {
        @JsonProperty("tenantId")

                private String tenantId = null;

        @JsonProperty("hierarchyType")

                private String hierarchyType = null;

        @JsonProperty("boundaryHierarchy")
          @Valid
                private List<BoundaryTypeHierarchy> boundaryHierarchy = null;


        public BoundaryTypeHierarchyDefinition addBoundaryHierarchyItem(BoundaryTypeHierarchy boundaryHierarchyItem) {
            if (this.boundaryHierarchy == null) {
            this.boundaryHierarchy = new ArrayList<>();
            }
        this.boundaryHierarchy.add(boundaryHierarchyItem);
        return this;
        }

}
