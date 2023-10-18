package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * BoundaryTypeHierarchyResponse
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-10-16T17:02:11.361704+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryTypeHierarchyResponse   {
        @JsonProperty("RequestInfo")

          @Valid
                private ResponseInfo requestInfo = null;

        @JsonProperty("BoundaryHierarchy")

          @Valid
                private BoundaryTypeHierarchyDefinition boundaryHierarchy = null;


}
