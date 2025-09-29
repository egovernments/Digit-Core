package org.egov.infra.mdms.utils;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
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

    private SchemaDefinitionService schemaDefinitionService;

    @Autowired
    public SchemaUtil(SchemaDefinitionService schemaDefinitionService){
        this.schemaDefinitionService = schemaDefinitionService;
    }

    /**
     * This method fetches the schema definition object for the master data being created.
     * @param mdmsRequest
     * @param clientId
     * @return
     */
    public JSONObject getSchema(MdmsRequest mdmsRequest, String clientId) {

        Mdms mdms = mdmsRequest.getMdms().get(0);

        SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder()
                .tenantId(mdms.getTenantId())
                .codes(Arrays.asList(mdms.getSchemaCode()))
                .build();

        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(SchemaDefSearchRequest.builder()
                .schemaDefCriteria(schemaDefCriteria).build());

        if(CollectionUtils.isEmpty(schemaDefinitions))
            throw new CustomException("SCHEMA_DEFINITION_NOT_FOUND_ERR", "Schema definition against which data is being created is not found");

        JSONObject schemaObject = new JSONObject(schemaDefinitions.get(0).getDefinition().toString());

        return schemaObject;
    }

}
