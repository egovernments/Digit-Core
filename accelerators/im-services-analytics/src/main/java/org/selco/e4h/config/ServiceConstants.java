package org.selco.e4h.config;


import org.springframework.stereotype.Component;


@Component
public class ServiceConstants {

	public static final String TENANTID_MDC_STRING = "TENANTID";

	//JSON Paths
	public static final String EMPLOYEE_PATH = "$.Employees.*";
	public static final String TENANT_PATH = "$.tenantId";
	public static final String IS_ACTIVE_PATH = "$.isActive";
	public static final String CREATED_DATE_PATH = "$.auditDetails.createdDate";
}
