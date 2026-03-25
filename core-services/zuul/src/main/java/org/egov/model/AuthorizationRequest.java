package org.egov.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.contract.Role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AuthorizationRequest {

    @NotNull
    @Size(min = 1)
    private Set<Role> roles;

    @NotNull
    private String uri;

    @NotNull
    private Set<String> tenantIds;

}
