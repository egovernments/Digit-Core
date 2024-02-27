package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Component
public class CredentialPayloadRequest {
    @JsonProperty("id")
    private String id;

    @JsonProperty("tradeName")
    private String tradeName;

    @JsonProperty("licenseType")
    private String licenseType;

    @JsonProperty("licenseNumber")
    private String licenseNumber;

    @JsonProperty("applicationNumber")
    private String applicationNumber;
}
