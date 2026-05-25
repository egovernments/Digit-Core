package org.egov.infra.mdms.utils;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MdmsCacheService;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SchemaUtil {

    private final SchemaDefinitionService schemaDefinitionService;
    private final MdmsCacheService mdmsCacheService;

    @Autowired
    public SchemaUtil(SchemaDefinitionService schemaDefinitionService, MdmsCacheService mdmsCacheService) {
        this.schemaDefinitionService = schemaDefinitionService;
        this.mdmsCacheService = mdmsCacheService;
    }

    /**
     * Fetches the schema definition for the given request, using Redis cache to avoid
     * a DB round-trip on every create/update. Schemas rarely change so TTL is long.
     */
    public JSONObject getSchema(MdmsRequest mdmsRequest) {
        Mdms mdms = mdmsRequest.getMdms();
        String tenantId = mdms.getTenantId();
        String schemaCode = mdms.getSchemaCode();

        SchemaDefinition cached = mdmsCacheService.getSchemaFromCache(tenantId, schemaCode);
        if (cached != null) {
            return new JSONObject(cached.getDefinition().toString());
        }

        SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder()
                .tenantId(tenantId)
                .codes(Arrays.asList(schemaCode))
                .build();

        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(SchemaDefSearchRequest.builder()
                .requestInfo(mdmsRequest.getRequestInfo())
                .schemaDefCriteria(schemaDefCriteria).build());

        if (CollectionUtils.isEmpty(schemaDefinitions))
            throw new CustomException("SCHEMA_DEFINITION_NOT_FOUND_ERR", "Schema definition against which data is being created is not found");

        SchemaDefinition schema = schemaDefinitions.get(0);
        mdmsCacheService.putSchemaToCache(tenantId, schemaCode, schema);

        return new JSONObject(schema.getDefinition().toString());
    }
}
