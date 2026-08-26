package org.egov.userevent.web.contract.v3;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event as exposed by the 3.0 API. Tenant comes from the X-Tenant-ID header;
 * internal-only fields (name, source, postedBy, referenceId, eventCategory,
 * recepientEventMap) are not part of this contract.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventV3 {

	private String id;

	@NotNull
	@Size(min = 2, max = 32)
	private String eventType;

	@NotNull
	@Size(min = 2, max = 1024)
	private String description;

	private EventStatusV3 status;

	private List<String> toRoles;

	private List<String> toUsers;

	@Valid
	private ActionV3 action;

	@Valid
	private EventDetailsV3 eventDetails;

	private AuditDetailV3 auditDetail;
}
