package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuditDetails(
    @JsonProperty("createdBy")        String createdBy,
    @JsonProperty("lastModifiedBy")   String lastModifiedBy,
    @JsonProperty("createdTime")      Long createdTime,
    @JsonProperty("lastModifiedTime") Long lastModifiedTime
) {
    public static AuditDetails now(String actor) {
        long ts = System.currentTimeMillis();
        return new AuditDetails(actor, actor, ts, ts);
    }
}
