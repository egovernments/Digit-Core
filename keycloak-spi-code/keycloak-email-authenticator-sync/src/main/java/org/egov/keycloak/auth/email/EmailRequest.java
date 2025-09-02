package org.egov.keycloak.auth.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
	@NotBlank(message = "templateId is required")
	private String templateId;

	@NotBlank(message = "version is required")
	private String version;

	@NotBlank(message = "tenantId is required")
	private String tenantId;

	@NotEmpty(message = "At least one emailId is required")
	private List<@Email(message = "Invalid email format") String> emailIds;

	private boolean enrich;

	private Map<String, Object> payload;

	private List<String> attachments;
}
