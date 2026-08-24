package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A registry schema as returned by the service. Mirrors its {@code Schema} record.
 *
 * <p>Note the key names differ between directions: the service serializes responses from record
 * fields, so the extension blocks come back as {@code xUnique}, {@code xRefSchema} and
 * {@code xIndexes}, whereas a create or update must send them hyphenated. That is why the request
 * shape is a separate type.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
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
public class RegistrySchema {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("schemaCode")
    private String schemaCode;
    @JsonProperty("version")
    private int version;
    @JsonProperty("definition")
    private JsonNode definition;
    @JsonProperty("xUnique")
    private List<List<String>> xUnique;
    @JsonProperty("xRefSchema")
    private List<RefSchema> xRefSchema;
    @JsonProperty("xIndexes")
    private List<SchemaIndex> xIndexes;
    @JsonProperty("webhook")
    private WebhookConfig webhook;
    @JsonProperty("isLatest")
    private boolean isLatest;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("createdTime")
    private Instant createdTime;
    @JsonProperty("modifiedTime")
    private Instant modifiedTime;
    @JsonProperty("createdBy")
    private String createdBy;
    @JsonProperty("modifiedBy")
    private String modifiedBy;
    @JsonProperty("requestId")
    private String requestId;
}
