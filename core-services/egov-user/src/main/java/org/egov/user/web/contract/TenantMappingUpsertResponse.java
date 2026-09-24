package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMappingUpsertResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("results")
    private List<Result> results;

    public enum Status {
        CREATED, ALREADY_PRESENT, USER_NOT_FOUND
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {

        /** The username or user id the caller asked for. */
        @JsonProperty("identifier")
        private String identifier;

        @JsonProperty("userId")
        private Long userId;

        @JsonProperty("status")
        private Status status;
    }
}
