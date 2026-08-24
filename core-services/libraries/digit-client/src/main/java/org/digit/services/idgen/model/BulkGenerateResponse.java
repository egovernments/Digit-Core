package org.digit.services.idgen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The generated ids, in the order they were allocated. */
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkGenerateResponse {
    @JsonProperty("templateCode")
    private String templateCode;
    @JsonProperty("version")
    private String version;
    @JsonProperty("count")
    private int count;
    @JsonProperty("ids")
    private List<String> ids;
}
