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
                        // Convert master data to JsonNode
                        JsonNode jsonNode = objectMapper.convertValue(masterDataJsonArray.get(0), JsonNode.class);

                        // Feed the converted master data to jsonSchemaInferrer for generating schema
                        JsonNode schemaNode = inferrer.inferForSample(jsonNode);

                        // Populate schemaCodeToSchemaJsonMap
                        schemaCodeToSchemaJsonMap.put(module + DOT_SEPARATOR + master, schemaNode);

                        // Write generated schema definition to files with the name in module.master format
                        fileWriter.writeJsonToFile(schemaNode, module + DOT_SEPARATOR + master);
                    }

                });
            });
        });
    }
}
