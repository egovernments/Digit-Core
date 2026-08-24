package org.digit.services.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A canonical (envelope) request: context plus payload.
 *
 * <p>The property order is load-bearing, not cosmetic. Registry resolves the acting tenant and user
 * by scanning the raw body for the first match of {@code "tenantId"} and of
 * {@code uuid|userId|id|userName|username}, so the metadata has to be serialized ahead of the
 * payload — otherwise a payload field of the same name silently wins.
 *
 * @param <T> the payload type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"RequestInfo", "data"})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalRequest<T> {
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
    @JsonProperty("data")
    private T data;
}
