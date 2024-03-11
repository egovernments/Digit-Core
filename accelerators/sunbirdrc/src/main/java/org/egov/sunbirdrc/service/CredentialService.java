package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.kafka.Producer;
import org.egov.sunbirdrc.models.CredentialIdUuidMapper;
import org.egov.sunbirdrc.repository.CredentialUuidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@Getter
@Setter
public class CredentialService {


    @Value("${sunbird.credential.url}")
    private String fetchCredentialUrl;

    @Value("${sunbird.save.vc.topic}")
    private String saveVcidTopic;

    @Autowired
    private MdmsSchemaService mdmsSchemaService;

    @Autowired
    private Producer producer;

    @Autowired
    private CredentialIdUuidMapper credentialIdUuidMapper;


    @Autowired
    private CredentialUuidRepository credentialUuidRepository;

    @Autowired
    private RevokeCredentialService revokeCredentialService;


    private String kafkaMessage;

    public void processPayloadAndPersistCredential(String KafkaConsumerPayload,String topic) throws JsonProcessingException {
        this.kafkaMessage = KafkaConsumerPayload;
        System.out.println("message receieved is" + kafkaMessage);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode requestPayload = objectMapper.readTree(this.kafkaMessage);
        String entityModuleName = requestPayload.path("module").asText();
        System.out.println("entity module name is " + entityModuleName);

        JsonNode mdmsModuleObject = mdmsSchemaService.getModuleDetailsFromMdmsData(entityModuleName);

        //get did, schemaId, uuid and jsonpath details from the mdms object fetched
        String entityDid= getDidFromModuleObject(mdmsModuleObject);
        String entitySchemaId=getSchemaIdFromModuleObject(mdmsModuleObject);
        String uuid=getSchemaIdFromModuleObject(mdmsModuleObject);
        List<String> listofJsonPaths= getAllFieldPathFromMdms(mdmsModuleObject);

        JsonNode payloadFromJsonPath= extractPayloadFromJsonPath(listofJsonPaths,requestPayload);
        System.out.println("the required mdms object is "+mdmsModuleObject);
        System.out.println("the entity id is "+entityDid);
        System.out.println("the schema id is "+entitySchemaId);
        System.out.println("the list of field json path is "+listofJsonPaths);
        if (topic.equals("recreate-vc")){
            CredentialIdUuidMapper credentialUuidObject=credentialUuidRepository.getRowData(uuid);
            String revokeApiResponse=revokeCredentialService.revokeCredential(credentialUuidObject.getCredentialId());
            JsonNode jsonNode = objectMapper.readTree(revokeApiResponse);
            String status = jsonNode.get("status").asText();
            if(status.equals("REVOKED")){
                String credentialIdUuidData=generateCredentials(uuid, entityDid, entitySchemaId,payloadFromJsonPath);
                producer.push("update-vcid", credentialIdUuidData);
            }
        }
        else{
            String credentialIdUuidData=generateCredentials(uuid, entityDid, entitySchemaId,payloadFromJsonPath);
            producer.push(saveVcidTopic, credentialIdUuidData);
        }
        //update cache after db update
        mdmsSchemaService.invalidateCache("vcid_uuid_mapper","vc-mdms");
        credentialUuidRepository.loadData();
    }


    public String getDidFromModuleObject(JsonNode mdmsModuleObject){
        if (mdmsModuleObject != null && mdmsModuleObject.has("definition")) {
            JsonNode definitionNode = mdmsModuleObject.get("definition");
            if (definitionNode != null && definitionNode.has("did")) {
                return definitionNode.get("did").asText();
            }
        }
        return null;
    }

    public String getSchemaIdFromModuleObject(JsonNode mdmsModuleObject){
        if (mdmsModuleObject != null && mdmsModuleObject.has("definition")) {
            JsonNode definitionNode = mdmsModuleObject.get("definition");
            if (definitionNode != null && definitionNode.has("schemaId")) {
                return definitionNode.get("schemaId").asText();
            }
        }
        return null;
    }

    public String getUuidFromModuleObject(JsonNode mdmsModuleObject){
        if (mdmsModuleObject != null && mdmsModuleObject.has("definition")) {
            JsonNode definitionNode = mdmsModuleObject.get("definition");
            if (definitionNode != null && definitionNode.has("uuid")) {
                return definitionNode.get("uuid").asText();
            }
        }
        return null;
    }

    public List<String> getAllFieldPathFromMdms(JsonNode mdmsModuleObject){
        ObjectMapper objectMapper = new ObjectMapper();
        if (mdmsModuleObject != null && mdmsModuleObject.has("definition")) {

            JsonNode definitionNode = mdmsModuleObject.get("definition");
            System.out.println("node value"+definitionNode);
            if (definitionNode != null && definitionNode.has("path")) {
                JsonNode fieldJsonPathList = definitionNode.get("path");
                System.out.println("path array value"+fieldJsonPathList);
                return objectMapper.convertValue(fieldJsonPathList, List.class);
            }
        }
        return null;
    }

    public JsonNode extractPayloadFromJsonPath(List<String> listofJsonPaths, JsonNode requestPayload){
        Map<String, String> fieldMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (String jsonPath : listofJsonPaths) {
            JsonNode node = requestPayload.at(jsonPath);
            if (!node.isMissingNode()) {
                fieldMap.put(jsonPath, node.asText());
            }
        }
        // Convert field map to JsonNode
        return objectMapper.convertValue(fieldMap, JsonNode.class);
    }



   public String generateCredentials(String uuid, String did, String schemaId,JsonNode payloadFromJsonPath) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

       String credentialPayload = objectMapper.writeValueAsString(payloadFromJsonPath);

       // Define the request body
       String requestBody = "{\n" +
               "    \"credential\": {\n" +
               "        \"@context\": [\n" +
               "            \"https://www.w3.org/2018/credentials/v1\",\n" +
               "            \"https://schema.org\"\n" +
               "        ],\n" +
               "        \"id\": \"" + did + "\",\n" +
               "        \"type\": [\n" +
               "            \"VerifiableCredential\",\n" +
               "            \"UniversityDegreeCredential\"\n" +
               "        ],\n" +
               "        \"issuer\": \"" + did + "\",\n" +
               "        \"expirationDate\": \"2023-02-08T11:56:27.259Z\",\n" +
               "        \"credentialSubject\": " + credentialPayload + ",\n" +
               "        \"options\": {\n" +
               "            \"created\": \"2020-04-02T18:48:36Z\",\n" +
               "            \"credentialStatus\": {\n" +
               "                \"type\": \"RevocationList2020Status\"\n" +
               "            }\n" +
               "        }\n" +
               "    },\n" +
               "    \"credentialSchemaId\": \"" + schemaId + "\",\n" +
               "    \"credentialSchemaVersion\": \"1.0.0\",\n" +
               "    \"tags\": [\"tag1\", \"tag2\", \"tag3\"]\n" +
               "}";

        // Create the request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Make the HTTP POST request
        ResponseEntity<String> response = restTemplate.exchange(fetchCredentialUrl, HttpMethod.POST, requestEntity, String.class);
        credentialIdUuidMapper.setCredentialId(getIdFromResponse(response));

        // Print the response
        System.out.println("Response status code: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        System.out.println(credentialIdUuidMapper.getCredentialId());
        credentialIdUuidMapper.setUuid(uuid);

        return objectMapper.writeValueAsString(credentialIdUuidMapper);

    }


    private String getIdFromResponse(ResponseEntity<String> response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode credentialNode = rootNode.path("credential");
        return credentialNode.path("id").asText();
    }

}
