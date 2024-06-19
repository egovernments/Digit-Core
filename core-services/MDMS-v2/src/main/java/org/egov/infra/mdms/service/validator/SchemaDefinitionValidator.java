package org.egov.infra.mdms.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.SchemaDefinitionRepository;
import static org.egov.infra.mdms.errors.ErrorCodes.*;
import static org.egov.infra.mdms.utils.MDMSConstants.*;
import org.egov.infra.mdms.utils.ErrorUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.*;

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
        Map<String, String> errors = new HashMap<>();

        // Validate schema attributes
        validateSchemaAttributes(schemaDefinition.getDefinition(), errors);

        // Check schema code uniqueness
        checkSchemaCode(schemaDefinition.getTenantId(), Arrays.asList(schemaDefinition.getCode()), errors);

        // Throw errors if any
        ErrorUtil.throwCustomExceptions(errors);
    }

    /**
     * This method performs validations on the schema definition attributes namely -
     * 1. validate that required fields list is not empty
     * 2. validate that unique fields list is not empty
     * 3. validate that list of unique attributes is a subset of list of required attributes
     * @param definition
     * @param errorMap
     */
    private void validateSchemaAttributes(JsonNode definition, Map<String, String> errorMap) {
        JSONObject schemaObject = new JSONObject(definition.toString());

        // Check if the incoming schema definition has "required" key and at least one value against it
        if(!schemaObject.has(REQUIRED_KEY) || ((org.json.JSONArray) schemaObject.get(REQUIRED_KEY)).length() == 0){
            errorMap.put(REQUIRED_ATTRIBUTE_LIST_ERR_CODE, REQUIRED_ATTRIBUTE_LIST_EMPTY_MSG);
        }

        // Check if the incoming schema definition has "x-unique" key and at least one value against it
        if(!schemaObject.has(X_UNIQUE_KEY) || ((org.json.JSONArray) schemaObject.get(X_UNIQUE_KEY)).length() == 0) {
            errorMap.put(UNIQUE_ATTRIBUTE_LIST_ERR_CODE, UNIQUE_ATTRIBUTE_LIST_EMPTY_MSG);
        }

        // Perform further validations iff both "required" and "x-unique" keys are present
        if(CollectionUtils.isEmpty(errorMap)) {
            List<Object> requiredAttributesList = ((org.json.JSONArray) schemaObject.get(REQUIRED_KEY)).toList();

            List<Object> uniqueAttributesList = ((org.json.JSONArray) schemaObject.get(X_UNIQUE_KEY)).toList();

            // Check if values against unique attributes are a subset of required fields
            if (uniqueAttributesList.size() > requiredAttributesList.size() || !requiredAttributesList.containsAll(uniqueAttributesList)) {
                errorMap.put(UNIQUE_ATTRIBUTE_LIST_ERR_CODE, UNIQUE_ATTRIBUTE_LIST_INVALID_MSG);
            }
        }
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
