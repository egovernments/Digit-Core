package org.digit.services.idgen.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An id template. Mirrors the idgen service's {@code TemplateResponse}.
 *
 * <p>Templates are versioned and immutable: a create makes {@code v1}, and each update makes a new
 * version rather than changing the existing one. The sequence counter belongs to the template code,
 * not the version, so updating a template does not restart its numbering.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdGenTemplate {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("templateCode")
    private String templateCode;
    /** Assigned by the service, in the form {@code v1}, {@code v2}, … */
    @JsonProperty("version")
    private String version;
    @JsonProperty("config")
    private TemplateConfig config;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
