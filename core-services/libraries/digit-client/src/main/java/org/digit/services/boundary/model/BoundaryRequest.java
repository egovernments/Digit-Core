package org.digit.services.boundary.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoundaryRequest {
    @JsonProperty(value="boundary")
    private List<Boundary> boundary;

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

    public List<Boundary> getBoundary() {
        return this.boundary;
    }

    public void setBoundary(List<Boundary> boundary) {
        this.boundary = boundary;
    }
}