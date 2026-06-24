package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

// Class (not record) because it needs to coexist with ErrorDetail structure while carrying extra fields
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorDetailDTO {

    private final ApiDetails apiDetails;
    private final List<ErrorEntity> errors;
    private final String uuid;
    private final AuditDetails auditDetails;
    private final Integer retryCount;
    private final Status status;

    private ErrorDetailDTO(Builder b) {
        this.apiDetails   = b.apiDetails;
        this.errors       = b.errors;
        this.uuid         = b.uuid;
        this.auditDetails = b.auditDetails;
        this.retryCount   = b.retryCount;
        this.status       = b.status;
    }

    public ApiDetails getApiDetails()     { return apiDetails; }
    public List<ErrorEntity> getErrors()  { return errors; }
    public String getUuid()               { return uuid; }
    public AuditDetails getAuditDetails() { return auditDetails; }
    public Integer getRetryCount()        { return retryCount; }
    public Status getStatus()             { return status; }

    public ErrorDetail toErrorDetail() { return new ErrorDetail(apiDetails, errors); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private ApiDetails apiDetails;
        private List<ErrorEntity> errors;
        private String uuid;
        private AuditDetails auditDetails;
        private Integer retryCount;
        private Status status;

        public Builder apiDetails(ApiDetails v)    { this.apiDetails = v; return this; }
        public Builder errors(List<ErrorEntity> v) { this.errors = v; return this; }
        public Builder uuid(String v)              { this.uuid = v; return this; }
        public Builder auditDetails(AuditDetails v){ this.auditDetails = v; return this; }
        public Builder retryCount(Integer v)       { this.retryCount = v; return this; }
        public Builder status(Status v)            { this.status = v; return this; }
        public ErrorDetailDTO build()              { return new ErrorDetailDTO(this); }
    }
}
