package org.egov.infra.mdms.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.tracer.model.CustomException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

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

    public String validate(MdmsRequest mdmsRequest) {
        // Initialize error map and fetch schema
        Map<String, String> errors = new HashMap<>();
        JSONObject schemaObject = getSchema(mdmsRequest);

        // Validations are performed here on the incoming data and its unique identifier is generated and returned
        validateDataWithSchemaDefinition(mdmsRequest, schemaObject, errors);
        String uniqueIdentifier = checkDuplicateAndReturnUniqueIdentifier(schemaObject, mdmsRequest);
        validateReference(schemaObject, mdmsRequest.getMdms());

        // Throw validation errors
        throwCustomException(errors);

        return uniqueIdentifier;
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
                    String message = validationException.getMessage();
                    errors.put("INVALID_REQUEST_".concat(validationException.getKeyword().toUpperCase()).concat(count.toString()), validationException.getErrorMessage());
                }
            } else {
                errors.put("INVALID_REQUEST", e.getErrorMessage());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private JSONObject getSchema(MdmsRequest mdmsRequest) {
        Mdms mdms = mdmsRequest.getMdms();
        SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder()
                .tenantId(mdms.getTenantId())
                .codes(Arrays.asList(mdms.getSchemaCode()))
                .build();
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(SchemaDefSearchRequest.builder()
                .requestInfo(mdmsRequest.getRequestInfo())
                .schemaDefCriteria(schemaDefCriteria).build());
        SchemaDefinition schemaDefinition = schemaDefinitions.get(0);
        JSONObject schemaObject = new JSONObject(schemaDefinition.getDefinition().toString());
        return schemaObject;
    }

    private String checkDuplicateAndReturnUniqueIdentifier(JSONObject schemaObject, MdmsRequest mdmsRequest) {
        String uniqueIdentifier = getUniqueIdentifier(schemaObject, mdmsRequest);

        Map<String, JSONArray> moduleMasterData = mdmsDataRepository.search(MdmsCriteria.builder()
                        .tenantId(mdmsRequest.getMdms().getTenantId())
                        .uniqueIdentifier(uniqueIdentifier)
                        .build());

        JSONArray masterData = moduleMasterData.get(mdmsRequest.getMdms().getSchemaCode());

        if (masterData != null && masterData.size() != 0) {
            throw new CustomException("DUPLICATE_RECORD", "Duplicate record");
        }

        return uniqueIdentifier;
    }

    private String getUniqueIdentifier(JSONObject schemaObject, MdmsRequest mdmsRequest) {
        org.json.JSONArray uniqueFieldPaths = (org.json.JSONArray) schemaObject.get("x-unique");

        JsonNode data = mdmsRequest.getMdms().getData();
        StringBuilder compositeUniqueIdentifier = new StringBuilder();

        // Build composite unique identifier
        IntStream.range(0, uniqueFieldPaths.length()).forEach(i -> {
            compositeUniqueIdentifier.append(data.at(getJsonPointerExpressionFromDotSeparatedPath(uniqueFieldPaths.getString(i))).asText());

            if (i != (uniqueFieldPaths.length() - 1))
                compositeUniqueIdentifier.append(".");
        });

        return compositeUniqueIdentifier.toString();
    }

    private String getJsonPointerExpressionFromDotSeparatedPath(String dotSeparatedPath) {
        return "/" + dotSeparatedPath.replaceAll("\\.", "/");
    }

    private void validateReference(JSONObject schemaObject, Mdms mdms) {
        if (schemaObject.has("x-ref-schema")) {
            org.json.JSONArray referenceSchema = (org.json.JSONArray) schemaObject.get("x-ref-schema");

            if (referenceSchema != null && referenceSchema.length() > 0) {
                Set<String> refSchemaUniqueIds = new HashSet<>();
                JsonNode mdmsData = mdms.getData();

                IntStream.range(0, referenceSchema.length()).forEach(i -> {
                    JSONObject jsonObject = referenceSchema.getJSONObject(i);
                    String refFieldPath = jsonObject.getString("fieldPath");
                    refSchemaUniqueIds.add(mdmsData.at(getJsonPointerExpressionFromDotSeparatedPath(refFieldPath)).asText());
                });

                List<Mdms> moduleMasterData = mdmsDataRepository.searchV2(
                        MdmsCriteriaV2.builder().tenantId(mdms.getTenantId()).ids(refSchemaUniqueIds).build());

                if (moduleMasterData.size() != refSchemaUniqueIds.size()) {
                    throw new CustomException("REFERENCE_VALIDATION_ERR", "Provided reference value does not exist in database");
                }
            }
        }
    }

    private void throwCustomException(Map<String, String> exceptions) {
        if (!exceptions.isEmpty()) {
            throw new CustomException(exceptions);
        }
    }

}
