package org.egov.access.web.contract.validateaction;

import jakarta.validation.Valid;
import lombok.*;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateActionContract {
	@NonNull
	@Valid
	private TenantRoleContract tenantRole;

	@NonNull
	private String actionUrl;
}
