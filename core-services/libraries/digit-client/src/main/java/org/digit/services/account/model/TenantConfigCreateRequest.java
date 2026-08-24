package org.digit.services.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload for creating a tenant configuration entry; the tenant comes from the header. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfigCreateRequest {
    @JsonProperty("configKey")
    private String configKey;
    @JsonProperty("configValue")
    private String configValue;
    @JsonProperty("description")
    private String description;
}
