package org.egov.userevent.web.contract.v3;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionV3 {

	private String id;

	private String eventId;

	@Valid
	@Size(min = 1, max = 100)
	private List<ActionItemV3> actionUrls;

	private AuditDetailV3 auditDetail;
}
