package com.example.gisService.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestBody {

    @JsonProperty("codes")
    @NonNull
    private List<String> codes ;

    @JsonProperty("tenantId")
    @NonNull
    private String tenantId ;
}
