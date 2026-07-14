package org.egov.access.domain.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Action {

	private Long id;

	@Size(max = 100)
	private String name;

	@Size(max = 100)
	private String url;

	@Size(max = 100)
	private String displayName;
	private Integer orderNumber;

	@Size(max = 100)
	private String queryParams;

	@Size(max = 50)
	private String parentModule;
	private boolean enabled;

	@Size(max = 50)
	private String serviceCode;

	@Size(max = 50)
	private String tenantId;

	private Date createdDate;

	private Long createdBy;

	private Date lastModifiedDate;

	private Long lastModifiedBy;

	private String path;

	private String navigationURL;
	private String leftIcon;
	private String rightIcon;

	/**
	 * Optional HTTP method this action applies to. Null = any method (legacy URL-only behavior).
	 */
	@Size(max = 20)
	private String method;

	/**
	 * Optional resource descriptor: a JSON object keyed by resource type (e.g. "complaint"), each
	 * holding a "attributes" object keyed by field path, each an independent
	 * {"condition": <JsonLogic>, "onDeny": {"strategy": ...}} rule — supports any number (N) of
	 * per-field visibility conditions per resource type. Legacy flat string-array shape
	 * (["complaint"]) is also accepted upstream and treated as "type only, no attribute rules".
	 * Null = no resource needed (legacy URL-only behavior). accesscontrol stores/returns this
	 * opaque JSON as-is; only the consuming service (e.g. pgr-services) interprets and validates
	 * its shape.
	 */
	private Object resource;

	/**
	 * Optional JsonLogic condition evaluated at runtime. Null = URL-match only (legacy behavior).
	 */
	private Object condition;

}
