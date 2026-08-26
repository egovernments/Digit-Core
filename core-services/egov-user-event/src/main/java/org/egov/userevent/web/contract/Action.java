package org.egov.userevent.web.contract;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.egov.tracer.annotations.CustomSafeHtml;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Validated
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class Action {
	@CustomSafeHtml
	private String tenantId;

	@CustomSafeHtml
	private String id;

	@CustomSafeHtml
	private String eventId;
	
	@NotNull
	private List<ActionItem> actionUrls;
	
}
