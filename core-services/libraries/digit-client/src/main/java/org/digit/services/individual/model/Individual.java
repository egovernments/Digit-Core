package org.digit.services.individual.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Individual {
    @JsonProperty("id")
    private String id;
    @JsonProperty("individualId")
    private String individualId;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("givenName")
    private String name;
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
    private Boolean mobileNumberVerified;
    @JsonProperty("altContactNumber")
    private String altContactNumber;
    @JsonProperty("email")
    private String email;
    @JsonProperty("emailVerified")
    private Boolean emailVerified;
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
    private Boolean isActive;
    @JsonProperty("version")
    private Integer version;
    @JsonProperty("address")
    private List<Address> address;
    @JsonProperty("documents")
    private List<Document> documents;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
    @JsonProperty("additionalAttributes")
    private Map<String, String> additionalAttributes;
}
