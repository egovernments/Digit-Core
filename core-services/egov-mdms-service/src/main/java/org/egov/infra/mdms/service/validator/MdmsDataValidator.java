package org.egov.infra.mdms.service.validator;

import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.impl.SchemaDefinitionRedisRepository;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MdmsDataValidator {



    private SchemaDefinitionService schemaDefinitionService;
    @Autowired
    public MdmsDataValidator(SchemaDefinitionService schemaDefinitionService) {
        this.schemaDefinitionService =  schemaDefinitionService;
    }

    public void validate(MdmsRequest mdmsRequest) {
        Mdms mdms = mdmsRequest.getMdms();
        SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder().tenantId(mdms.getTenantId()).codes(Arrays.asList(mdms.getSchemaCode())).build();
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(SchemaDefSearchRequest.builder().requestInfo(mdmsRequest.getRequestInfo()).schemaDefCriteria(schemaDefCriteria).build());
        SchemaDefinition schemaDefinition = schemaDefinitions.get(0);
        JSONObject jsonSchema = new JSONObject(schemaDefinitions.get(0).getDefinition());
        Schema schema = SchemaLoader.load(jsonSchema);
        schema.validate(new JSONObject(mdmsRequest.getMdms().getData()));
    }

    private void validateSchemaCode(String schemaCode) {

        //Path param schema matches to the body schema code (may not be required if override)

        //is Schema exist in schema registry

    }

    private void validateDataSchemaWithSchemaDefinition(Mdms mdms) {
        //List<SchemaDefinition>  schemaDefinitions = schemaDefinitionRedisRepository.read(mdms.getTenantId(), Arrays.asList(mdms.getSchemaCode()));
        //Schema schema = SchemaLoader.load(schemaDefinitions.get(0).getDefinition())
    }

}
