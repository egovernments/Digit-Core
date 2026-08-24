package org.digit.services.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * A single rejected item of a bulk request.
 *
 * @param index position of the item in the submitted list, so the caller can map it back to its input
 * @param errors why it was rejected
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public record BulkFailure(int index, List<DigitError> errors) {
}
