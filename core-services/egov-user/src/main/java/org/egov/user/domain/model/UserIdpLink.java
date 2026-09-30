package org.egov.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdpLink {

    private String tenantId;
    private String issuer;
    private String subject;
    private Long userId;
    private String uuid;
    private String providerId;
    private Date createdDate;
}
