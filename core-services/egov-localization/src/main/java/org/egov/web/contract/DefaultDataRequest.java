package org.egov.web.contract;

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
public class DefaultDataRequest {
    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("targetTenantId")
    @NotNull
    @Valid
    private String targetTenantId = null;

    @JsonProperty("locale")
    @NotNull
    @Valid
    private String locale = null;

    @JsonProperty("modules")
    @NotNull
    @Valid
    private List<String> modules = null;
}
