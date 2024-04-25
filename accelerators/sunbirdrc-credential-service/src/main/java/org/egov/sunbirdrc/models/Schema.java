package org.egov.sunbirdrc.models;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
