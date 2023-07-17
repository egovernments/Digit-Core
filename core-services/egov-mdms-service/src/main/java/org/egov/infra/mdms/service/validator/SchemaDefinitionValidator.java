package org.egov.infra.mdms.service.validator;

import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.infra.mdms.model.SchemaDefSearchRequest;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import static org.egov.infra.mdms.errors.ErrorCodes.*;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SchemaDefinitionValidator {


    private SchemaDefinitionService schemaDefinitionService;

    @Autowired
    public SchemaDefinitionValidator( @Lazy SchemaDefinitionService schemaDefinitionService) {
        this.schemaDefinitionService = schemaDefinitionService;
    }

    public void validateCreateRequest(SchemaDefinitionRequest schemaDefinitionRequest) {
        SchemaDefinition schemaDefinition = schemaDefinitionRequest.getSchemaDefinition();
        RequestInfo requestInfo = schemaDefinitionRequest.getRequestInfo();
        Map<String, String> errors = new HashMap<>();
        checkSchemaCode(requestInfo, schemaDefinition.getTenantId(), Arrays.asList(schemaDefinition.getCode()), errors);
        throwCustomException(errors);
    }

    private void checkSchemaCode(RequestInfo requestInfo, String tenantId, List<String> codes, Map<String, String> errorMap) {
        SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder().tenantId(tenantId).codes(codes).build();
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(SchemaDefSearchRequest.builder().requestInfo(requestInfo).schemaDefCriteria(schemaDefCriteria).build());
        if(!schemaDefinitions.isEmpty()){
            errorMap.put(DUPLICATE_SCHEMA_CODE, DUPLICATE_SCHEMA_CODE_MSG);
        }
    }

    private void throwCustomException(Map<String, String> exceptions) {
        if (!exceptions.isEmpty()) {
            throw new CustomException(exceptions);
        }
    }
}
