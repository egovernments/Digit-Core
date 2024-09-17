package com.example.boundarymigration.web.models.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-10-16T17:02:11.361704+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Boundary {

    @JsonProperty("id")
    private String id;

    @JsonProperty("boundaryNum")
    private String boundaryNum;

    @JsonProperty("name")
    private String name;

    @JsonProperty("localname")
    private String localname;

    @JsonProperty("longitude")
    private String longitude;

    @JsonProperty("latitude")
    private String latitude;

    @JsonProperty("label")
    private String label;

    @JsonProperty("code")
    private String code;

    @JsonProperty("children")
    private List<Boundary> children;
}
