package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DefaultMasterDataRequest {
    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("TargetTenantId")
    @NotNull
    @Valid
    private String targetTenantId = null;

    @JsonProperty("SchemaCodes")
    @NotNull
    @Valid
    private List<String> schemaCodes = null;
}