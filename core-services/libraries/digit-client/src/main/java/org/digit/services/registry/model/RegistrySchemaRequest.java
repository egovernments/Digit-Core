package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A schema to register or replace.
 *
 * <p>Separate from {@link RegistrySchema} because the wire names differ by direction: a write must
 * send the extension blocks hyphenated ({@code x-unique}, {@code x-ref-schema}, {@code x-indexes}),
 * while a read returns them camel-cased. Only {@code schemaCode} and {@code definition} are required;
 * the tenant comes from the request context, not the payload.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrySchemaRequest {
    @JsonProperty("schemaCode")
    private String schemaCode;
    /** A JSON Schema document describing a record. */
    @JsonProperty("definition")
    private JsonNode definition;
    /** Field combinations to enforce as unique; each inner list is one composite key. */
    @JsonProperty("x-unique")
    private List<List<String>> xUnique;
    @JsonProperty("x-ref-schema")
    private List<RefSchema> xRefSchema;
    @JsonProperty("x-indexes")
    private List<SchemaIndex> xIndexes;
    @JsonProperty("webhook")
    private WebhookConfig webhook;
}
