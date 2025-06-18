package org.egov.pgr.web.models.Notification;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Validated
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class Action {
	
	private String tenantId;
	
	private String id;
	
	private String eventId;
	
	@NotNull
	private List<ActionItem> actionUrls;
	
}
