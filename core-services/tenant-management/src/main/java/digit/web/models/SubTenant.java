package digit.web.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Details of a subTenant
 */
@Schema(description = "Details of a subTenant")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubTenant {

    @JsonProperty("id")
    @Size(min = 2, max = 128)
    private String id = null;

    @JsonProperty("code")
    @Size(min = 1, max = 100)
    private String code = null;

    @JsonProperty("tenantId")
    @Size(min = 1, max = 100)
    @NotNull
    private String tenantId = null;

    // alphanumeric / alphabet
    @JsonProperty("name")
    @NotNull
    @Size(min = 1, max = 100)
    private String name = null;

    @JsonProperty("email")
    private String email = null;

    @JsonProperty("additionalAttributes")
    private Object additionalAttributes = null;

    @JsonProperty("isActive")
    private Boolean isActive = Boolean.TRUE;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

}
