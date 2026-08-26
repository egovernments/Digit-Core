package org.egov.userevent.web.contract.v3;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Spec's shared AuditDetail schema. Uses modifiedBy/modifiedTime (not the
 * internal lastModifiedBy/lastModifiedTime naming).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditDetailV3 {

	private String createdBy;

	private Long createdTime;

	private String modifiedBy;

	private Long modifiedTime;
}
