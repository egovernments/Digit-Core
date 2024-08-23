package org.egov.mdms_schema.config;

import org.springframework.stereotype.Component;

@Component
public class ServiceConstants {

	public static final String EXTERNAL_SERVICE_EXCEPTION = "External Service threw an Exception: ";
	public static final String SEARCHER_SERVICE_EXCEPTION = "Exception while fetching from searcher: ";

	public static final String ERROR_WHILE_FETCHING_FROM_MDMS = "Exception occurred while fetching category lists from mdms: ";

	public static final String RES_MSG_ID = "uief87324";
	public static final String SUCCESSFUL = "successful";
	public static final String FAILED = "failed";
}
