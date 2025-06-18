package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.BoundaryRelation;
import digit.web.models.ResponseInfo;
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
 * BoundaryRelationshipResponse
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-14T17:06:34.078752728+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryRelationshipResponse   {
        @JsonProperty("ResponseInfo")

          @Valid
                private ResponseInfo responseInfo = null;

        @JsonProperty("TenantBoundary")
          @Valid
                private List<BoundaryRelation> tenantBoundary = null;


        public BoundaryRelationshipResponse addTenantBoundaryItem(BoundaryRelation tenantBoundaryItem) {
            if (this.tenantBoundary == null) {
            this.tenantBoundary = new ArrayList<>();
            }
        this.tenantBoundary.add(tenantBoundaryItem);
        return this;
        }

}
