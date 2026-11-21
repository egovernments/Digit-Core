package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * A single boundary relationship that could not be created during a bulk request, paired with
 * the reason it failed so the caller can act on it (for example, by idempotent resubmission).
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailedBoundaryRelationship {

    @JsonProperty("boundaryRelationship")
    @Valid
    private BoundaryRelation boundaryRelationship = null;

    @JsonProperty("errorCode")
    private String errorCode = null;

    @JsonProperty("errorMessage")
    private String errorMessage = null;

}
