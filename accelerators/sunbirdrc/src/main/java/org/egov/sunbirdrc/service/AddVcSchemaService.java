package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AddVcSchemaService {

    private final String SCHEMA_ENDPOINT = "http://localhost:3333/credential-schema";

    public String addVcSchemaToMdms(String mdmsSchema) throws JsonProcessingException {
        Map<String, Object> payloadMap = new ObjectMapper().readValue(mdmsSchema, Map.class);

        // Remove the "mdmsData" from the payload
        payloadMap.remove("mdmsData");
        // Create a new HttpHeaders object
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a new HttpEntity with the modified payload and headers
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payloadMap, headers);

        // Make a POST request to the schema endpoint
        ResponseEntity<String> responseEntity = new RestTemplate().exchange(SCHEMA_ENDPOINT, HttpMethod.POST, requestEntity, String.class);

        // Print the response received from the request
        System.out.println("Response from schema endpoint: " + responseEntity.getBody());

        return responseEntity.getBody();
    }
}
