package org.egov.infra.mdms.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
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

    public void validate(MdmsRequest mdmsRequest) {
        log.info("MdmsDataValidator validate()");
        Map<String, String> errors = new HashMap<>();
        JSONObject schemaObject = getSchema(mdmsRequest);
        validateDataWithSchemaDefinition(mdmsRequest, schemaObject, errors);
        checkDuplicate(schemaObject, mdmsRequest);
        throwCustomException(errors);
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

    //TODO: Check nested path(a.b.c)
    //TODO: Implement Composite unique id
    private void checkDuplicate(JSONObject schemaObject, MdmsRequest mdmsRequest) {
        org.json.JSONArray uniqueFields = (org.json.JSONArray) schemaObject.get("x-unique");
        JsonNode data = mdmsRequest.getMdms().getData();
        String uniqueIdetifier = data.get(uniqueFields.getString(0)).asText();
        Map<String, JSONArray> moduleMasterData = mdmsDataRepository.search(MdmsCriteria.builder()
                .tenantId(mdmsRequest.getMdms()
                        .getTenantId()).uniqueIdentifier(uniqueIdetifier).build());
        JSONArray masterData = moduleMasterData.get(mdmsRequest.getMdms().getSchemaCode());
        if( masterData != null && masterData.size() != 0) {
            throw new CustomException("DUPLICATE_RECORD","Duplicate record");
        }
        mdmsRequest.getMdms().setUniqueIdentifier(uniqueIdetifier);
    }
    private void validateReference(JSONObject schemaObject, Mdms mdms) {
        org.json.JSONArray referenceSchema = (org.json.JSONArray) schemaObject.get("x-ref-schema");

        if(referenceSchema != null && referenceSchema.length()>0) {
            Set<String> refSchemaUniqueIds = new HashSet<>();
            JsonNode mdmsData = mdms.getData();

            for (int i = 0; i < referenceSchema.length(); i++) {
                JSONObject jsonObject = referenceSchema.getJSONObject(i);
                String refFieldPath = jsonObject.getString("fieldPath");
                String[] nodePaths = refFieldPath.split("\\.");
                collectUniqueIds(mdmsData, nodePaths, 0, refSchemaUniqueIds);
            }

            Map<String, JSONArray> moduleMasterData = mdmsDataRepository.search(
                    MdmsCriteria.builder().tenantId(mdms.getTenantId()).ids(refSchemaUniqueIds).build());
        }
    }

    private void collectUniqueIds(JsonNode currentNode, String[] nodePaths, int index, Set<String> refSchemaUniqueIds) {
        if (index == nodePaths.length - 1) {
            return;
        }

        String nodePath = nodePaths[index];
        JsonNode nextNode = currentNode.get(nodePath);

        if (nextNode != null) {
            String uniqueId = nextNode.asText();
            refSchemaUniqueIds.add(uniqueId);
            collectUniqueIds(nextNode, nodePaths, index + 1, refSchemaUniqueIds);
        }
    }

    private void throwCustomException(Map<String, String> exceptions) {
        if (!exceptions.isEmpty()) {
            throw new CustomException(exceptions);
        }
    }



}
