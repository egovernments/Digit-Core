package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * egov-idgen id request. Defined locally because the shared org.egov.common.contract.idgen.IdRequest
 * has no count field, and count is needed to generate ids in bulk with a single call.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdRequest {

    @JsonProperty("idName")
    private String idName;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("format")
    private String format;

    @JsonProperty("count")
    private Integer count;

}
