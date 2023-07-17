package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Bind the request meta data(RequestInfo) and Schema defination
 */
@Schema(description = "Bind the request meta data(RequestInfo) and Schema defination")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@RedisHash("SchemaDefinition")
public class SchemaDefinition implements Serializable{

    @JsonProperty("id")
    @Size(min = 2, max = 128)
    private String id = null;

    @JsonProperty("tenantId")
    @NotNull
    @Size(min = 2, max = 128)
    private String tenantId = null;

    @JsonProperty("code")
    @NotNull
    @Size(min = 2, max = 128)
    @Id
    private String code = null;

    @JsonProperty("description")

    @Size(min = 2, max = 512)
    private String description = null;

    @JsonProperty("definition")
    @NotNull
    private JsonNode definition = null;

    @JsonProperty("isActive")
    private Boolean isActive = true;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

}
