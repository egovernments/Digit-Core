package org.egov.pg.models;

import lombok.*;

/**
 * DIGIT 3.0 audit details; uses modifiedBy/modifiedTime unlike the legacy
 * tracer AuditDetails (lastModifiedBy/lastModifiedTime).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuditDetail {

	private String createdBy;

	private Long createdTime;

	private String modifiedBy;

	private Long modifiedTime;
}
