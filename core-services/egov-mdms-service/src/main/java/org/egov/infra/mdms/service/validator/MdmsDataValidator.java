package org.egov.infra.mdms.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.infra.mdms.utils.CompositeUniqueIdentifierGenerationUtil;
import org.egov.infra.mdms.utils.ErrorUtil;
import org.egov.tracer.model.CustomException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.IntStream;
import static org.egov.infra.mdms.utils.MDMSConstants.*;

@Component
@Slf4j
public class MdmsDataValidator {

    private final SchemaDefinitionService schemaDefinitionService;

    private final MdmsDataRepository mdmsDataRepository;

    @Autowired
    public MdmsDataValidator(SchemaDefinitionService schemaDefinitionService, MdmsDataRepository mdmsDataRepository) {
        this.schemaDefinitionService = schemaDefinitionService;
        this.mdmsDataRepository = mdmsDataRepository;
    }

    public void validate(MdmsRequest mdmsRequest, JSONObject schemaObject) {
        // Initialize error map and fetch schema
        Map<String, String> errors = new HashMap<>();

        // Validations are performed here on the incoming data
        validateDataWithSchemaDefinition(mdmsRequest, schemaObject, errors);
        checkDuplicate(schemaObject, mdmsRequest);
        validateReference(schemaObject, mdmsRequest.getMdms());

        // Throw validation errors
        ErrorUtil.throwCustomExceptions(errors);
    }

    public void validateSearchRequest(){

    }

    private void validateSchemaCode(String schemaCode, MdmsRequest mdmsRequest) {
        //Path param schema matches to the body schema code (may not be required if override)
        //is Schema exist in schema registry
    }

    private void validateDataWithSchemaDefinition(MdmsRequest mdmsRequest, JSONObject schemaObject,Map<String, String> errors) {
        try {
            JSONObject dataObject = new JSONObject(mdmsRequest.getMdms().getData().toString());

            Schema schema = SchemaLoader.load(schemaObject);
            schema.validate(dataObject);
        } catch (ValidationException e) {
            Integer count = 0;
            if (!e.getCausingExceptions().isEmpty()) {
                for (ValidationException validationException : e.getCausingExceptions()) {
                    ++count;
                    errors.put("INVALID_REQUEST_".concat(validationException.getKeyword().toUpperCase()).concat(count.toString()), validationException.getErrorMessage());
                }
            } else {
                errors.put("INVALID_REQUEST", e.getErrorMessage());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void checkDuplicate(JSONObject schemaObject, MdmsRequest mdmsRequest) {
        String uniqueIdentifier = CompositeUniqueIdentifierGenerationUtil.getUniqueIdentifier(schemaObject, mdmsRequest);

        Map<String, JSONArray> moduleMasterData = mdmsDataRepository.search(MdmsCriteria.builder()
                        .tenantId(mdmsRequest.getMdms().getTenantId())
                        .uniqueIdentifier(uniqueIdentifier)
                        .build());

        JSONArray masterData = moduleMasterData.get(mdmsRequest.getMdms().getSchemaCode());

        if (masterData != null && masterData.size() != 0) {
            throw new CustomException("DUPLICATE_RECORD", "Duplicate record");
        }

    }

    private void validateReference(JSONObject schemaObject, Mdms mdms) {
        if (schemaObject.has(X_REFERENCE_SCHEMA_KEY)) {
            org.json.JSONArray referenceSchema = (org.json.JSONArray) schemaObject.get(X_REFERENCE_SCHEMA_KEY);

            if (referenceSchema != null && referenceSchema.length() > 0) {
                Set<String> refSchemaUniqueIds = new HashSet<>();
                JsonNode mdmsData = mdms.getData();

                IntStream.range(0, referenceSchema.length()).forEach(i -> {
                    JSONObject jsonObject = referenceSchema.getJSONObject(i);
                    String refFieldPath = jsonObject.getString(FIELD_PATH_KEY);
                    refSchemaUniqueIds.add(mdmsData.at(CompositeUniqueIdentifierGenerationUtil.getJsonPointerExpressionFromDotSeparatedPath(refFieldPath)).asText());
                });

                List<Mdms> moduleMasterData = mdmsDataRepository.searchV2(
                        MdmsCriteriaV2.builder().tenantId(mdms.getTenantId()).ids(refSchemaUniqueIds).build());

                if (moduleMasterData.size() != refSchemaUniqueIds.size()) {
                    throw new CustomException("REFERENCE_VALIDATION_ERR", "Provided reference value does not exist in database");
                }
            }
        }
    }

}
