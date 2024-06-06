package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerSchema {
    @JsonProperty("$id")
    private String id;
    @JsonProperty("$schema")
    private String schema;
    private String description;
    private String type;
    private Map<String, Object> properties;
    private List<String> required;
    private boolean additionalProperties;
}
