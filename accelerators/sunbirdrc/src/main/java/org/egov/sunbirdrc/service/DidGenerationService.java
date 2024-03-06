package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.sunbirdrc.kafka.Producer;
import org.egov.sunbirdrc.models.CredentialIdUuidMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class DidGenerationService {
    @Value("${sunbird.did.generation.url}")
    private String generateDidUrl;

    @Autowired
    private CredentialIdUuidMapper credentialIdUuidMapper;

    @Autowired
    private Producer producer;

    private String issuerDid;
    public void generateDidForIssuer(String tradeLicensePayload) throws JsonProcessingException {
        System.out.println("message receieved for did generation for issuer is"+ tradeLicensePayload);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(tradeLicensePayload);
        String uuid = jsonNode.path("Data").path("tradelicense").path("id").asText();
        System.out.println("Extracted ID from the json payload to generate did for the issuer: " + uuid);
        generateDid(uuid);
    }

    public void generateDid(String uuid) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate restTemplate= new RestTemplate();
        String payload = "{\n" +
                "    \"content\": \n" +
                "        [\n" +
                "            {\n" +
                "                \"alsoKnownAs\": [\"" + uuid + "\", \"test@gmail.com\"],\n" +
                "                \"services\": [\n" +
                "                    {\n" +
                "                        \"id\": \"IdentityHub\",\n" +
                "                        \"type\": \"IdentityHub\",\n" +
                "                        \"serviceEndpoint\": {\n" +
                "                            \"@context\": \"schema.identity.foundation/hub\",\n" +
                "                            \"@type\": \"UserServiceEndpoint\",\n" +
                "                            \"instance\": [\n" +
                "                                \"did:test:hub.id\"\n" +
                "                            ]\n" +
                "                        }\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"method\": \"upai\"\n" +
                "            }\n" +
                "        ]\n" +
                "}";


        System.out.println("ddi request payload is"+payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Set the request entity, including headers and payload
        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
        // Send the POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(generateDidUrl, HttpMethod.POST, requestEntity, String.class);
        System.out.println("Response for did generation is: " + responseEntity.getBody());
        credentialIdUuidMapper.setIssuerDid(getDidFromResponseObject(responseEntity.getBody()));
        credentialIdUuidMapper.setUuid(uuid);
        String jsonData=objectMapper.writeValueAsString(credentialIdUuidMapper);
      //  producer.push("save-did", jsonData);

    }

    public String getDidFromResponseObject(String responsePayload){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responsePayload);
            issuerDid=jsonNode.get(0).get("id").asText();
            System.out.println("First id: " + issuerDid);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return issuerDid;
    }
}
