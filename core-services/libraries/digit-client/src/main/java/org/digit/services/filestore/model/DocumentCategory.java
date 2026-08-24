package org.digit.services.filestore.model;

import org.digit.services.common.model.AuditDetails;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A document category. Mirrors the service's {@code DocumentCategory}, including its unusual key
 * casing — {@code ID} and {@code TenantId} are capitalised, the size bounds are strings, and the
 * audit block is singular {@code auditDetail} while the field is plural.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/*
 * Jackson would otherwise publish each is-prefixed boolean twice: once under the name its
 * @JsonProperty gives it, and again under the name inferred from Lombok's isX() getter — so
 * {"isActive":false,"active":false}. Services that parse strictly reject the second key outright,
 * which made every individual write fail; the rest silently ignored it. Suppressing is-getter
 * detection leaves the annotated fields as the single source of the wire contract.
 */
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class DocumentCategory {
    @JsonProperty("ID")
    private long id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("TenantId")
    private String tenantId;
    @JsonProperty("code")
    private String code;
    @JsonProperty("allowedFormats")
    private List<String> allowedFormats;
    @JsonProperty("minSize")
    private String minSize;
    @JsonProperty("maxSize")
    private String maxSize;
    @JsonProperty("isSensitive")
    private Boolean isSensitive;
    @JsonProperty("description")
    private String description;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("version")
    private long version;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetails;
}
