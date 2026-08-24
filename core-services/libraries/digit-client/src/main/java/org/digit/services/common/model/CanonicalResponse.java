package org.digit.services.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A canonical (envelope) response.
 *
 * <p>Only the metadata and payload are modelled: services differ in what else they put alongside
 * them — some merge the legacy body into the root, some nest everything under {@code data} — and
 * unknown keys are ignored, so this reads correctly against all of them.
 *
 * @param <T> the payload type
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalResponse<T> {
    /** Left untyped: the metadata block's own field set varies between services. */
    @JsonProperty("ResponseMetadata")
    private Map<String, Object> responseMetadata;
    @JsonProperty("data")
    private T data;
}
