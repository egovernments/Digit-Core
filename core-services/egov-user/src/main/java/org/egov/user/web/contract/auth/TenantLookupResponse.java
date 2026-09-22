package org.egov.user.web.contract.auth;

import lombok.Builder;
import lombok.Getter;
import org.egov.user.domain.model.UserTenantMapping;

import java.util.List;

@Getter
@Builder
public class TenantLookupResponse {
    private String username;
    private List<UserTenantMapping> tenants;
}
