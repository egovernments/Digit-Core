package org.egov.userevent.web.contract;

import jakarta.validation.constraints.NotNull;

import org.egov.tracer.annotations.CustomSafeHtml;
import org.springframework.validation.annotation.Validated;

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
public class ActionItem {
	
	@NotNull
	@CustomSafeHtml
	private String actionUrl;
	
	@NotNull
	@CustomSafeHtml
	private String code;

}
