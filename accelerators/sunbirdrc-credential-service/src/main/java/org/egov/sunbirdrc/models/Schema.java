package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String type;
    private String version;
    private String id;
    private String name;
    private String author;
    private String authored;
    private InnerSchema schema;
    private List<String> tags;
    private String status;
    private String deletedAt; // Assuming deletedAt can be of any type, adjust accordingly
    private String createdBy; // Assuming createdBy can be of any type, adjust accordingly
    private String updatedBy;
}
