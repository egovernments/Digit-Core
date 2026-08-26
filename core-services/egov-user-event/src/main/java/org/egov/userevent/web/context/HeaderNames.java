package org.egov.userevent.web.context;

/** Header names from the 3.0 API contract (digitnxt common.yaml). */
public final class HeaderNames {

	public static final String TENANT_ID = "X-Tenant-ID";
	public static final String USER_ID = "X-User-ID";
	public static final String REQUEST_ID = "X-Request-ID";
	public static final String CORRELATION_ID = "X-Correlation-ID";
	public static final String RESPONSE_TIME = "X-Response-Time";
	public static final String RESPONSE_TIMESTAMP = "X-Response-Timestamp";
	public static final String AUTHORIZATION = "Authorization";

	private HeaderNames() {
	}
}
