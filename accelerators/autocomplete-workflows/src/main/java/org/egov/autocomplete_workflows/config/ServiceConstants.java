package org.egov.autocomplete_workflows.config;

import org.springframework.stereotype.Component;

@Component
public class ServiceConstants {

	public static final String WF_AUTO_APPROVE_ACTION = "AUTO-APPROVE";
	public static final String WF_AUTO_REJECT_ACTION = "AUTO-REJECT";

	public static final String WF_APPROVE_ACTION = "APPROVE";
	public static final String WF_REJECT_ACTION = "REJECT";

	public static final String WF_APPROVED_STATE = "APPROVED";
	public static final String WF_REJECTED_STATE = "REJECTED";

}
