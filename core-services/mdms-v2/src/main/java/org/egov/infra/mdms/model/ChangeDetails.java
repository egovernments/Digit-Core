package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeDetails {

    @JsonProperty("historyId")
    private Long historyId = null;

    @JsonProperty("version")
    private Integer version = null;

    @JsonProperty("operation")
    private HistoryOperation operation = null;

    @JsonProperty("changedTime")
    private Long changedTime = null;

    @JsonProperty("dbUser")
    private String dbUser = null;

    @JsonProperty("clientAddress")
    private String clientAddress = null;

    @JsonProperty("applicationName")
    private String applicationName = null;

    @JsonProperty("transactionId")
    private Long transactionId = null;
}
