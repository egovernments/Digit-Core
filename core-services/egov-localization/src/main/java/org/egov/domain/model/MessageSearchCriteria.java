package org.egov.domain.model;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Set;

import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchCriteria {
	
	private Tenant tenantId;

	@Size(max = 255)
	private String locale;

	@Size(max = 255)
	private String module;
	
	private Set<String> codes;

	public boolean isModuleAbsent() {
		return isEmpty(module);
	}
}
