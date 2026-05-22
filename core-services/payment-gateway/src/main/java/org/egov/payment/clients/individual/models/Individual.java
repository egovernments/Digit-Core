package org.egov.payment.clients.individual.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.AuditDetails;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Individual {

    @JsonProperty("id")
    private String id;

    @JsonProperty("individualId")
    private String individualId;

    @JsonProperty("clientReferenceId")
    private String clientReferenceId;

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

    @JsonProperty("bloodGroup")
    private String bloodGroup;

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

    @JsonProperty("zoneInfo")
    private String zoneInfo;

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("picture")
    private String picture;

    @JsonProperty("profile")
    private String profile;

    @JsonProperty("website")
    private String website;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("fatherName")
    private String fatherName;

    @JsonProperty("husbandName")
    private String husbandName;

    @JsonProperty("relationship")
    private String relationship;

    @JsonProperty("photo")
    private String photo;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userUuid")
    private String userUuid;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("type")
    private String type;

    @JsonProperty("roles")
    private List<Role> roles;

    @JsonProperty("isSystemUser")
    private Boolean isSystemUser;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    @JsonProperty("createdTime")
    private Long createdTime;

    @JsonProperty("lastModifiedTime")
    private Long lastModifiedTime;

    @JsonProperty("rowVersion")
    private Integer rowVersion;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
