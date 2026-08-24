package org.digit.services.notification.model;

import org.digit.services.common.model.AuditDetails;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A message template. Mirrors the notification service's {@code Template}.
 *
 * <p>Templates are versioned: each update publishes a new version rather than editing in place, so
 * a message already sent keeps rendering from the version it used.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("templateId")
    private String templateId;
    @JsonProperty("version")
    private String version;
    /** EMAIL or SMS. */
    @JsonProperty("type")
    private String type;
    /** Email only; SMS has no subject. */
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("content")
    private String content;
    @JsonProperty("isHTML")
    private boolean isHTML;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
