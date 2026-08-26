package org.egov.userevent.web.contract.v3;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Spec's shared Error schema; responses carry a bare JSON array of these. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorV3 {

	private String code;

	private String message;

	private String description;

	private List<String> params;
}
