package org.egov.tracer.constants;

public class TracerConstants {

    public static final String CORRELATION_ID_HEADER = "x-correlation-id";
    public static final String TENANT_ID_HEADER = "tenantId";
    public static final String CORRELATION_ID_FIELD_NAME= "correlationId";
    public static final String CORRELATION_ID_MDC = "CORRELATION_ID";
    public static final String TENANTID_MDC = "TENANTID";
    public static final String CORRELATION_ID_OPENTRACING_FORMAT = "correlation.id";
    public static final String TIME_ZONE_PROPERTY = "app.timezone";
    public static final String REQUEST_INFO_FIELD_NAME_IN_JAVA_CLASS_CASE = "RequestInfo";
    public static final String REQUEST_INFO_IN_CAMEL_CASE = "requestInfo";
    public static final String UNHANDLED_EXCEPTION_ERROR_CODE = "UNHANDLED_EXCEPTION_ERROR";
    public static final String UNABLE_TO_RETRIEVE_REQUEST_BODY_MSG = "Unable to retrieve request body";
    public static final String UTF_8_CODE = "UTF-8";
    public static final String EXCEPTION_CAUGHT_IN_TRACER_MSG = "Exception caught in tracer ";

    private TracerConstants() {
    }
}
