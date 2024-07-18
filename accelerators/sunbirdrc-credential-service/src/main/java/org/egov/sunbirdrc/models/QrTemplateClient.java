package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrTemplateClient {

    @JsonProperty("schemaId")
    private String schemaId;

    @JsonProperty("schemaVersion")
    private String schemaVersion;

    @JsonProperty("template")
    private String template;

    @JsonProperty("type")
    private String type;
}
