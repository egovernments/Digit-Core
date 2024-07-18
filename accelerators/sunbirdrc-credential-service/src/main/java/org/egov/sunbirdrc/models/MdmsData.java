package org.egov.sunbirdrc.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MdmsData {

    @JsonProperty("uuid")
    @Valid
    private String uuid;

    @JsonProperty("path")
    @Valid
    private List<Object> path;

    @JsonProperty("code")
    @Valid
    private String code;

    @JsonProperty("context")
    @Valid
    private JsonNode context;

    @JsonProperty("expiryDate")
    @Valid
    private String expiryDate;
}
