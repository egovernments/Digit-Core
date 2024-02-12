package org.egov.auditservice.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.auditservice.web.models.enums.OperationType;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class AuditLog {

  @JsonProperty("id")
  private String id = null;

  @NotNull
  @JsonProperty("userUUID")
  private String userUUID = null;

  @NotNull
  @JsonProperty("module")
  private String module = null;

  @NotNull
  @JsonProperty("tenantId")
  private String tenantId = null;

  @JsonProperty("transactionCode")
  private String transactionCode = null;

  @NotNull
  @JsonProperty("changeDate")
  private Long changeDate = null;

  @NotNull
  @JsonProperty("entityName")
  private String entityName = null;

  @NotNull
  @JsonProperty("objectId")
  private String objectId = null;

  @JsonProperty("auditCorrelationId")
  private String auditCorrelationId = null;

  @NotNull
  @JsonProperty("keyValueMap")
  private Map<String, Object> keyValueMap = null;

  @NotNull
  @JsonProperty("operationType")
  private OperationType operationType = null;

  @JsonProperty("integrityHash")
  private String integrityHash = null;

}
