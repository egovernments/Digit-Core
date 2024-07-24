package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCodeRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("code")
    private String code;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("tenantId")
    private String tenantId;

}
