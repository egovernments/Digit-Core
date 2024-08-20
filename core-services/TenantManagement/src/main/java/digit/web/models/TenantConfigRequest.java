package digit.web.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * Details of Tenant Configuration
 */
@Schema(description = "Details of Tenant Configuration")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantConfigRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo RequestInfo;

//    @JsonProperty("defaultLoginType")
//    private TenantConfig.DefaultLoginTypeEnum defaultLoginType = null;

    @JsonProperty("enableUserBasedLogin")
    private Boolean enableUserBasedLogin = null;

    @JsonProperty("additionalAttributes")
    private Object additionalAttributes = null;

    @JsonProperty("id")
    @Valid
    private UUID id = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

}
