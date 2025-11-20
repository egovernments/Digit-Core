package com.digit.services.boundary.model;

import com.digit.services.account.model.ResponseInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response wrapper for Boundary operations.
 * Based on the actual API structure from Go service.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoundaryResponse {

    @JsonProperty("responseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("boundary")
    private List<Boundary> boundary;

    // Explicit getter/setter methods as fallback for Lombok
    public ResponseInfo getResponseInfo() {
        return responseInfo;
    }

    public void setResponseInfo(ResponseInfo responseInfo) {
        this.responseInfo = responseInfo;
    }

    public List<Boundary> getBoundary() {
        return boundary;
    }

    public void setBoundary(List<Boundary> boundary) {
        this.boundary = boundary;
    }
}
