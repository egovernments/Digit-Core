package org.egov.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Domain model for SSO/IDP session and MFA details stored in eg_user_idp_details.
 * Separated from eg_user to avoid full user row updates on every SSO login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdpDetails {

    private Long id;
    private String tenantId;
    private String uuid;

    private Date idpTokenExp;
    private Date lastSsoLoginAt;
    private String tokenId;

    private Boolean mfaEnabled;
    private String mfaDeviceName;
    private String mfaPhoneLast4;
    private Date mfaRegisteredOn;
    private String mfaDetails;

    private Date createdDate;
    private Date lastModifiedDate;

    private Long createdBy;
    private Long lastModifiedBy;
}
