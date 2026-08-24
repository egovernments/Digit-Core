package org.digit.services.idgen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Asks for several ids from one template in a single call. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkGenerateRequest {
    @JsonProperty("templateCode")
    private String templateCode;
    /** 1..1000. */
    @JsonProperty("count")
    private Integer count;
    /** Values for any placeholders the template declares. */
    @JsonProperty("variables")
    private Map<String, String> variables;
}
