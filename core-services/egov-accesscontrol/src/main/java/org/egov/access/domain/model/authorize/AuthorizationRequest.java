package org.egov.access.domain.model.authorize;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthorizationRequest {

    @NotNull
    @Size(min = 1)
    private Set<Role> roles;

    @NotNull
    private String uri;

    @NotNull
    @Size(min = 1)
    private Set<String> tenantIds;

}
