package com.digit.services.boundary.model;

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
 * Request wrapper for Boundary operations.
 * Based on the actual API structure from Postman collection.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoundaryRequest {

    @JsonProperty("boundary")
    private List<Boundary> boundary;

    // Explicit builder methods as fallback for Lombok
    public static BoundaryRequest builder() {
        return new BoundaryRequest();
    }

    public BoundaryRequest boundary(List<Boundary> boundary) {
        this.boundary = boundary;
        return this;
    }

    public BoundaryRequest build() {
        return this;
    }

    // Explicit getter/setter methods
    public List<Boundary> getBoundary() {
        return boundary;
    }

    public void setBoundary(List<Boundary> boundary) {
        this.boundary = boundary;
    }
}
