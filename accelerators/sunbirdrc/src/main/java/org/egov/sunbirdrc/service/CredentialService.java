package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.models.DidSchemaId;
import org.egov.sunbirdrc.models.VcCredentialId;
import org.egov.sunbirdrc.repository.DidSchemaIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class CredentialService {


    @Autowired
    private DidSchemaIdRepository didSchemaIdRepository;

    @Autowired
    private VcCredentialId vcCredentialId;

    @Autowired
    private DidSchemaId didSchemaId;

    private String kafkaMessage;
    public void processMessageFromKafka(String kafkaMessage) throws JsonProcessingException {
        this.kafkaMessage=kafkaMessage;
        System.out.println("message receieved is"+ kafkaMessage);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(this.kafkaMessage);
        String id = jsonNode.path("Data").path("tradelicense").path("id").asText();
        System.out.println("Extracted ID from the json payload is: " + id);

        DidSchemaId didSchemaIdMap= didSchemaIdRepository.getRowData(id);
        VcCredentialId vcCredentialId= getVcCredentialsId(didSchemaIdMap.getDid(), didSchemaIdMap.getSchemaId());

        System.out.println("credential id is "+vcCredentialId.getVcCredentialId());

    }

    public VcCredentialId getVcCredentialsId(String did, String schemaId) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        // Define the URL
        String url = "http://localhost:3000/credentials/issue";

        // Define the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define the request body
        String requestBody = "{\n" +
                "    \"credential\": {\n" +
                "        \"@context\": [\n" +
                "            \"https://www.w3.org/2018/credentials/v1\",\n" +
                "            \"https://www.w3.org/2018/credentials/examples/v1\"\n" +
                "        ],\n" +
                "        \"id\": \"did:upai:2a3de7e3-5c1a-4f15-a837-7f7ff960a75a\",\n" +
                "        \"type\": [\n" +
                "            \"VerifiableCredential\",\n" +
                "            \"UniversityDegreeCredential\"\n" +
                "        ],\n" +
                "        \"issuer\": \"did:upai:2a3de7e3-5c1a-4f15-a837-7f7ff960a75a\",\n" +
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
                "    \"credentialSchemaId\": \"did:schema:5fe6aa08-4546-4dc4-9b2b-989303d1b01a\",\n" +
                "    \"credentialSchemaVersion\": \"1.0.0\",\n" +
                "    \"tags\": [\"tag1\", \"tag2\", \"tag3\"]\n" +
                "}";

        // Create the request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Make the HTTP POST request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        vcCredentialId=getIdFromResponse(response);

        // Print the response
        System.out.println("Response status code: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        return vcCredentialId; // Wrap the string in a ResponseEntity and return
    }

    private VcCredentialId getIdFromResponse(ResponseEntity<String> response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode credentialNode = rootNode.path("credential");
        String CredentialsId= credentialNode.path("id").asText();
        vcCredentialId.setVcCredentialId(CredentialsId);
        return vcCredentialId;
    }

}
