package org.egov.infra.mdms.service.validator;

import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.infra.mdms.model.SchemaDefSearchRequest;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.infra.mdms.repository.SchemaDefinitionRepository;
import org.egov.infra.mdms.repository.impl.SchemaDefinitionDbRepositoryImpl;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import static org.egov.infra.mdms.errors.ErrorCodes.*;

import org.egov.infra.mdms.utils.ErrorUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SchemaDefinitionValidator {

    private SchemaDefinitionRepository schemaDefinitionRepository;

    @Autowired
    public SchemaDefinitionValidator(SchemaDefinitionRepository schemaDefinitionRepository) {
        this.schemaDefinitionRepository = schemaDefinitionRepository;
    }

    /**
     * This method performs business validations on schemaDefinitionRequest.
     * @param schemaDefinitionRequest
     */
    public void validateCreateRequest(SchemaDefinitionRequest schemaDefinitionRequest) {

        SchemaDefinition schemaDefinition = schemaDefinitionRequest.getSchemaDefinition();
        RequestInfo requestInfo = schemaDefinitionRequest.getRequestInfo();
        Map<String, String> errors = new HashMap<>();

        // Check schema code uniqueness
        checkSchemaCode(schemaDefinition.getTenantId(), Arrays.asList(schemaDefinition.getCode()), errors);

        // Throw errors if any
        ErrorUtil.throwCustomExceptions(errors);
    }

    /**
     * This method checks whether a schema definition with the provided tenantId and code already exists.
     * @param tenantId
     * @param codes
     * @param errorMap
     */
    private void checkSchemaCode(String tenantId, List<String> codes, Map<String, String> errorMap) {

        // Build schema definition search criteria
        SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder().tenantId(tenantId).codes(codes).build();

        // Search for schema definitions with the incoming tenantId and codes
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionRepository.search(schemaDefCriteria);

        // If schema definition already exists for tenantId and code combination, populate error map
        if(!schemaDefinitions.isEmpty()){
            errorMap.put(DUPLICATE_SCHEMA_CODE, DUPLICATE_SCHEMA_CODE_MSG);
        }
    }

}
