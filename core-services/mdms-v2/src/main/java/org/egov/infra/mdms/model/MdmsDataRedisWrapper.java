package org.egov.infra.mdms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONArray;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsDataRedisWrapper {

    private String tenantSchemaCode;

    private Map<String, JSONArray> schemaCodeData;
}
