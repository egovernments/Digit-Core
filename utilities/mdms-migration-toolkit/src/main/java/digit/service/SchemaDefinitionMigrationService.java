package digit.service;

import static digit.constants.MDMSMigrationToolkitConstants.DOT_SEPARATOR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasquatch.jsonschemainferrer.*;

import digit.repository.ServiceRequestRepository;
import digit.util.FileReader;
import digit.util.FileWriter;
import digit.web.models.SchemaDefinition;
import digit.web.models.SchemaDefinitionRequest;
import digit.web.models.SchemaMigrationRequest;
import net.minidev.json.JSONArray;

@Service
public class SchemaDefinitionMigrationService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileWriter fileWriter;

    @Autowired
    private FileReader fileReader;

    @Autowired
    private JsonSchemaInferrer inferrer;

    @Value("${master.schema.files.dir}")
    public String schemaFilesDirectory;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    private Map<String, JsonNode> schemaCodeToSchemaJsonMap;

    public void beginMigration(SchemaMigrationRequest schemaMigrationRequest) {
        // Fetch schema code to schema definition map
        Map<String, JsonNode> schemaCodeVsSchemaDefinitionMap = fileReader.readFiles(schemaFilesDirectory);

        List<SchemaDefinition> schemaDefinitionPOJOs = new ArrayList<>();

        // Go through each schemas and generate SchemaDefinition DTOs
        schemaCodeVsSchemaDefinitionMap.keySet().forEach(schemaCode -> {
            SchemaDefinition schemaDefinition = SchemaDefinition.builder()
                    .tenantId(schemaMigrationRequest.getSchemaMigrationCriteria().getTenantId())
                    .isActive(Boolean.TRUE)
                    .code(schemaCode)
                    .definition(schemaCodeVsSchemaDefinitionMap.get(schemaCode))
                    .id(UUID.randomUUID().toString())
                    .build();
            schemaDefinitionPOJOs.add(schemaDefinition);
        });

        schemaDefinitionPOJOs.forEach(schemaDefinition -> {
            SchemaDefinitionRequest schemaDefinitionRequest = SchemaDefinitionRequest.builder()
                    .requestInfo(schemaMigrationRequest.getRequestInfo())
                    .schemaDefinition(schemaDefinition)
                    .build();

            // Send it to kafka/make API calls to MDMS service schema APIs
            serviceRequestRepository.fetchResult(new StringBuilder("http://localhost:8094/mdms-v2/schema/v1/_create"), schemaDefinitionRequest);
        });
    }

    /**
     * Adds "x-unique" field and required fields (ONLY from master-config uniqueKeys) to the schema definition
     * @param definition The original schema definition JsonNode
     * @param uniqueIdentifierFields List of unique identifier field names from master-config.json
     * @return Updated JsonNode with "x-unique" and required fields (both same as uniqueKeys from master-config)
     */
    private JsonNode addXUniqueToDefinition(JsonNode definition, List<String> uniqueIdentifierFields) {
        try {
            // Convert JsonNode to ObjectNode so we can modify it
            com.fasterxml.jackson.databind.node.ObjectNode objectNode = (com.fasterxml.jackson.databind.node.ObjectNode) definition;

            // Create required array and unique array - both contain the same fields from master-config uniqueKeys
            com.fasterxml.jackson.databind.node.ArrayNode requiredArray = objectMapper.createArrayNode();
            com.fasterxml.jackson.databind.node.ArrayNode uniqueArray = objectMapper.createArrayNode();

            // Add unique identifier fields to both required and x-unique arrays
            for (String field : uniqueIdentifierFields) {
                requiredArray.add(field);
                uniqueArray.add(field);
            }

            // Set the required array (only unique fields from master-config)
            objectNode.set("required", requiredArray);

            // Add "x-unique" field to the schema definition
            objectNode.set("x-unique", uniqueArray);

            return objectNode;
        } catch (Exception e) {
            System.err.println("Error adding x-unique to definition: " + e.getMessage());
            return definition;
        }
    }

    /**
     * Extracts unique identifier fields from master config for a given schema code
     * Converts JSONPath expressions (e.g., "$.code") to field names (e.g., "code")
     * @param schemaCode Schema code in format "module.master"
     * @param masterConfigMap Master configuration map
     * @return List of unique identifier field names
     */
    private List<String> getUniqueIdentifierFields(String schemaCode, Map<String, Map<String, Object>> masterConfigMap) {
        List<String> uniqueIdentifierFields = new ArrayList<>();

        // Parse schemaCode (format: "module.master")
        String[] parts = schemaCode.split("\\.");
        if (parts.length != 2) {
            // Default fallback if format is unexpected
            uniqueIdentifierFields.add("code");
            return uniqueIdentifierFields;
        }

        String module = parts[0];
        String master = parts[1];

        // Check if master config exists for this module and master
        if (masterConfigMap.containsKey(module) && masterConfigMap.get(module).containsKey(master)) {
            try {
                Object masterConfig = masterConfigMap.get(module).get(master);
                String masterConfigJson = objectMapper.writeValueAsString(masterConfig);

                // Extract uniqueKeys array from master config
                List<String> uniqueKeys = com.jayway.jsonpath.JsonPath.read(masterConfigJson, "$.uniqueKeys");

                if (uniqueKeys != null && !uniqueKeys.isEmpty()) {
                    // Convert JSONPath expressions to field names
                    for (String jsonPath : uniqueKeys) {
                        // Remove "$." prefix to get field name
                        String fieldName = jsonPath.replaceFirst("^\\$\\.", "");
                        uniqueIdentifierFields.add(fieldName);
                    }
                }
            } catch (Exception e) {
                // Log error and continue with default
                System.err.println("Error extracting unique keys for " + schemaCode + ": " + e.getMessage());
            }
        }

        // If no unique keys found, use "code" as default
        if (uniqueIdentifierFields.isEmpty()) {
            uniqueIdentifierFields.add("code");
            System.out.println("Using default uniqueIdentifierFields [code] for schema: " + schemaCode);
        } else {
            System.out.println("Extracted uniqueIdentifierFields " + uniqueIdentifierFields + " for schema: " + schemaCode);
        }

        return uniqueIdentifierFields;
    }

    public void generateSchemaDefinition() {
        Map<String, Map<String, Map<String, JSONArray>>> tenantMap = MDMSApplicationRunnerImpl.getTenantMap();

        schemaCodeToSchemaJsonMap = new HashMap<>();

        // Traverse tenantMap across the tenants, modules and masters to generate schema for each master
        tenantMap.keySet().forEach(tenantId -> {
            tenantMap.get(tenantId).keySet().forEach(module -> {
                tenantMap.get(tenantId).get(module).keySet().forEach(master -> {
                    JSONArray masterDataJsonArray = MDMSApplicationRunnerImpl
                            .getTenantMap()
                            .get(tenantId)
                            .get(module)
                            .get(master);

                    if (!masterDataJsonArray.isEmpty()) {
                        // Convert all records in masterDataJsonArray to a List of JsonNodes
                        List<JsonNode> jsonNodeList = new ArrayList<>();
                        for (Object masterDatum : masterDataJsonArray) {
                            JsonNode jsonNode = objectMapper.convertValue(masterDatum, JsonNode.class);
                            jsonNodeList.add(jsonNode);
                        }

                        // Feed ALL records to jsonSchemaInferrer for generating comprehensive schema
                        JsonNode schemaNode = inferrer.inferForSamples(jsonNodeList);

                        // Get schema code for extracting unique fields
                        String schemaCode = module + DOT_SEPARATOR + master;

                        // Extract unique identifier fields from master config
                        List<String> uniqueIdentifierFields = getUniqueIdentifierFields(schemaCode, MDMSApplicationRunnerImpl.getMasterConfigMap());

                        // Add x-unique, required fields, title, and x-ref-schema
                        JsonNode updatedSchemaNode = addXUniqueToDefinition(schemaNode, uniqueIdentifierFields);
                        com.fasterxml.jackson.databind.node.ObjectNode schemaObjectNode =
                            (com.fasterxml.jackson.databind.node.ObjectNode) updatedSchemaNode;
                        schemaObjectNode.put("title", "Generated schema for " + master);
                        schemaObjectNode.set("x-ref-schema", objectMapper.createArrayNode());

                        // Remove additionalProperties field to match MDMS v2 schema format
                        schemaObjectNode.remove("additionalProperties");

                        // Populate schemaCodeToSchemaJsonMap
                        schemaCodeToSchemaJsonMap.put(schemaCode, schemaObjectNode);

                        // Write generated schema definition to files with the name in module.master format
                        fileWriter.writeJsonToFile(schemaObjectNode, schemaCode);
                    }

                });
            });
        });
    }
}
