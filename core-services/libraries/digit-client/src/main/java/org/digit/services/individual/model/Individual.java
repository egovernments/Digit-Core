package org.digit.services.individual.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors the individual service's {@code IndividualDTO}. Field types follow that DTO exactly,
 * including its primitives.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Individual {
    @JsonProperty("id")
    private String id;
    @JsonProperty("individualId")
    private String individualId;
    // No tenantId: the service takes the tenant from the X-Tenant-ID header, its DTO declares no
    // such field, and it parses writes with a mapper that rejects unknown keys.
    @JsonProperty("givenName")
    private String givenName;
    @JsonProperty("familyName")
    private String familyName;
    @JsonProperty("otherNames")
    private String otherNames;
    @JsonProperty("dateOfBirth")
    private String dateOfBirth;
    @JsonProperty("gender")
    private String gender;
    @JsonProperty("age")
    private Integer age;
    @JsonProperty("mobileNumber")
    private String mobileNumber;
    @JsonProperty("mobileNumberVerified")
    private boolean mobileNumberVerified;
    @JsonProperty("altContactNumber")
    private String altContactNumber;
    @JsonProperty("email")
    private String email;
    @JsonProperty("emailVerified")
    private boolean emailVerified;
    @JsonProperty("locale")
    private String locale;
    @JsonProperty("fatherName")
    private String fatherName;
    @JsonProperty("husbandName")
    private String husbandName;
    @JsonProperty("photo")
    private String photo;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("version")
    private int version;
    @JsonProperty("requestId")
    private String requestId;
    /** Serialized as {@code address}, matching the service, whose field is likewise plural. */
    @JsonProperty("address")
    private List<Address> address;
    @JsonProperty("identifiers")
    private List<Identifier> identifiers;
    @JsonProperty("documents")
    private List<Document> documents;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
    /**
     * Values are arbitrary JSON, not strings: the service declares {@code Map<String, Object>}, so a
     * nested object or a number here previously failed to deserialize.
     */
    @JsonProperty("additionalAttributes")
    private Map<String, Object> additionalAttributes;
}
