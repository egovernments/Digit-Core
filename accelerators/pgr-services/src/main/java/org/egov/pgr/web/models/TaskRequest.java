package org.egov.pgr.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import org.egov.common.contract.request.RequestInfo;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import java.util.List;

public class TaskRequest {

    @NonNull
    @Valid
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @SafeHtml
    @JsonProperty("judgeId")
    private String judgeId;

    @SafeHtml
    @JsonProperty("advocateId")
    private String advocateId;

    @SafeHtml
    @JsonProperty("litigantIds")
    private List<String> litigantIds;

    @SafeHtml
    @JsonProperty("tenantId")
    private String tenantId;

    @SafeHtml
    @JsonProperty("cnrNumber")
    private String cnrNumber;
}
