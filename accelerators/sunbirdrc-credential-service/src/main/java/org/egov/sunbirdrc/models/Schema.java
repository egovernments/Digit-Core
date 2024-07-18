package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schema {

    @JsonProperty("type")
    private String type;

    @JsonProperty("version")
    private String version;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("author")
    private String author;

    @JsonProperty("authored")
    private String authored;

    @JsonProperty("schema")
    private InnerSchema schema;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("status")
    private String status;

    @JsonProperty("deletedAt")
    private String deletedAt; // Assuming deletedAt can be of any type, adjust accordingly

    @JsonProperty("createdBy")
    private String createdBy; // Assuming createdBy can be of any type, adjust accordingly

    @JsonProperty("updatedBy")
    private String updatedBy;
}
