package org.egov.user.web.contract;

import lombok.*;
import org.egov.user.domain.model.Role;

@Getter
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"code", "tenantId"})
public class RoleRequest {

    private String code;
    private String name;
    private String tenantId;

    public RoleRequest(Role domainRole) {
        this.code = domainRole.getCode();
        this.name = domainRole.getName();
        this.tenantId = domainRole.getTenantId();
    }

    public Role toDomain() {
        return Role.builder()
                .code(code)
                .name(name)
                .tenantId(tenantId)
                .build();
    }
}
