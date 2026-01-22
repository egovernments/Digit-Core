package org.egov.user.domain.model.hrms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@EqualsAndHashCode(exclude = {"auditDetails"})
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class ReactivationDetails {

	private String id;

	@NotNull
	private String reasonForReactivation;

	private String orderNo;

	private String remarks;

	@NotNull
	private Long effectiveFrom;

	private String tenantId;

	private AuditDetails auditDetails;
}
