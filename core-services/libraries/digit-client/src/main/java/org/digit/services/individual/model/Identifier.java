package org.digit.services.individual.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An identity document reference on an individual. Mirrors the service's {@code IdentifierDTO}.
 *
 * <p>{@code identifierType} is validated server-side against a fixed set — currently
 * {@code NATIONAL_ID}, {@code AADHAAR}, {@code PASSPORT}, {@code VOTER_ID}, {@code PAN},
 * {@code DRIVING_LICENSE} and {@code SYSTEM_GENERATED}. Left as a String rather than an enum so a
 * newly permitted type does not require a new release of this library.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Identifier {
    @JsonProperty("id")
    private String id;
    @JsonProperty("individualId")
    private String individualId;
    @JsonProperty("identifierType")
    private String identifierType;
    @JsonProperty("identifierId")
    private String identifierId;
    @JsonProperty("verified")
    private boolean verified;
    @JsonProperty("documentType")
    private String documentType;
    @JsonProperty("fileStoreId")
    private String fileStoreId;
    @JsonProperty("active")
    private boolean active;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
