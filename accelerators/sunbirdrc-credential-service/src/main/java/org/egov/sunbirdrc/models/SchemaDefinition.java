package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
@Builder
public class SchemaDefinition {


    private String tenantId;

    private String code;

    private String description;

    private ObjectNode definition;

    private Boolean isActive;
}
