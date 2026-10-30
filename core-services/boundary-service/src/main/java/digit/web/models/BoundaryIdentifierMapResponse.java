package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Search response: boundary code -> identifierId
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryIdentifierMapResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("BoundaryIdentifier")
    private Map<String, String> boundaryIdentifier = null;

}
