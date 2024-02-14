package com.example.gateway.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.Role;


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
