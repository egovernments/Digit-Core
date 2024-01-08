package com.example.geoJsonService.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class Properties {
    @JsonProperty("id")
    private String id =null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("code")
    private String code = null;
}
