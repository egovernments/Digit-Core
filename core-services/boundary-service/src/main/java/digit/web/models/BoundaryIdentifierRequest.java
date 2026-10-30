package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * BoundaryIdentifierRequest
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryIdentifierRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo = null;

    // Either an explicit list of identifiers, or (on _create only) GenerateCriteria to auto-generate them.
    @Valid
    @JsonProperty("BoundaryIdentifier")
    private List<BoundaryIdentifier> boundaryIdentifier = null;

    @Valid
    @JsonProperty("GenerateCriteria")
    private BoundaryIdentifierGenerateCriteria generateCriteria = null;

}
