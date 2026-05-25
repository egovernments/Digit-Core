package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.EnrichedBoundary;
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
 * EnrichedBoundary
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-14T17:06:34.078752728+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrichedBoundary   {
        @JsonProperty("id")

                private String id = null;

        @JsonProperty("tenantId")

                private String tenantId = null;

        @JsonProperty("code")
          @NotNull

                private String code = null;

        @JsonProperty("boundaryType")

                private String boundaryType = null;

        @JsonProperty("children")
          @Valid
                private List<EnrichedBoundary> children = null;


        public EnrichedBoundary addChildrenItem(EnrichedBoundary childrenItem) {
            if (this.children == null) {
            this.children = new ArrayList<>();
            }
        this.children.add(childrenItem);
        return this;
        }

}
