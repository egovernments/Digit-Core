package org.digit.services.account.model;

import org.digit.services.common.model.AuditDetails;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A tenant as returned by the account service. Mirrors its {@code TenantResponse}. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @JsonProperty("id")
    private String id;
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("address")
    private String address;
    @JsonProperty("city")
    private String city;
    @JsonProperty("state")
    private String state;
    @JsonProperty("pincode")
    private String pincode;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("passwordGenerated")
    private boolean passwordGenerated;
    @JsonProperty("additionalAttributes")
    private Map<String, Object> additionalAttributes;
    @JsonProperty("version")
    private int version;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
