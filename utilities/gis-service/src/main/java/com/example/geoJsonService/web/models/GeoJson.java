package com.example.geoJsonService.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
@Getter
@Setter
public class GeoJson {

    @JsonProperty("type")
    private String type =null;

    @JsonProperty("features")
    private List<Feature> features =null;

}
