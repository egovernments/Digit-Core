package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.kafka.Producer;
import org.egov.sunbirdrc.models.CredentialIdUuidMapper;
import org.egov.sunbirdrc.models.CredentialPayloadRequest;
import org.egov.sunbirdrc.models.DidSchemaId;
import org.egov.sunbirdrc.repository.DidSchemaIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CredentialService {


    @Value("${sunbird.credential.url}")
    private String fetchCredentialUrl;

    @Value("${sunbird.save.vc.topic}")
    private String saveVcidTopic;
    @Autowired
    private DidSchemaIdRepository didSchemaIdRepository;

    @Autowired
    private MdmsSchemaService mdmsSchemaService;

    @Autowired
    private DidSchemaId didSchemaId;

    @Autowired
    private Producer producer;

    @Autowired
    private CredentialIdUuidMapper credentialIdUuidMapper;

    @Autowired
    private CredentialPayloadRequest credentialPayloadRequest;

    private String kafkaMessage;
    public void processMessageFromKafka(String kafkaMessage) throws JsonProcessingException {
        this.kafkaMessage=kafkaMessage;
        System.out.println("message receieved is"+ kafkaMessage);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(this.kafkaMessage);
        String uuid = jsonNode.path("Data").path("tradelicense").path("id").asText();
        System.out.println("Extracted ID from the json payload is: " + uuid);

        DidSchemaId didSchemaIdMap= didSchemaIdRepository.getRowData(uuid);
        String vcCredentialId= getVcCredentialsId(uuid,didSchemaIdMap.getDid(), didSchemaIdMap.getSchemaId());

        System.out.println("credential id is "+vcCredentialId);

    }

    public String getVcCredentialsId(String uuid, String did, String schemaId) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        credentialPayloadRequest=mdmsSchemaService.getJsonPathMdmsSchema("tradelicense-mdms");

        // Define the request body
        String requestBody = "{\n" +
                "    \"credential\": {\n" +
                "        \"@context\": [\n" +
                "            \"https://www.w3.org/2018/credentials/v1\",\n" +
                "            \"https://www.w3.org/2018/credentials/examples/v1\"\n" +
                "        ],\n" +
                "        \"id\": \"" + did + "\",\n" +
                "        \"type\": [\n" +
                "            \"VerifiableCredential\",\n" +
                "            \"UniversityDegreeCredential\"\n" +
                "        ],\n" +
                "        \"issuer\": \"" + did + "\",\n" +
                "        \"expirationDate\": \"2023-02-08T11:56:27.259Z\",\n" +
                "        \"credentialSubject\": {\n" +
                "            \"id\": \"did:upai:928896a9-7a05-41e3-b787-151680f03e4e\",\n" +
                "            \"grade\": \"9.23\",\n" +
                "            \"programme\": \"B.Tech\",\n" +
                "            \"certifyingInstitute\": \"IIIT Sonepat\",\n" +
                "            \"evaluatingInstitute\": \"NIT Kurukshetra\"\n" +
                "        },\n" +
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
        String jsonData=objectMapper.writeValueAsString(credentialIdUuidMapper);
        producer.push(saveVcidTopic, jsonData);

        return credentialIdUuidMapper.getCredentialId(); // Wrap the string in a ResponseEntity and return

    }



    private String getIdFromResponse(ResponseEntity<String> response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode credentialNode = rootNode.path("credential");
        return credentialNode.path("id").asText();

    }

}
