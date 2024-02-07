package org.egov.tracer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetailDTO extends ErrorDetail{
    private String uuid;
    private AuditDetails auditDetails;
    private Integer retryCount;
    private Status status;

}
