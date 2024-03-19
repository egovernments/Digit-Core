package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.unix.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AddVcSchemaService {

    private final String SCHEMA_ENDPOINT = "http://localhost:3333/credential-schema";
    private final String mdmsRequestUrl = "http://localhost:9002/mdms-v2/schema/v1/_create";
    private JsonNode mdmsDataRequestPayload;
    private JsonNode credentialSchemaPayload;
    private ObjectNode vcSchemaPayload;

    @Autowired
    private MdmsSchemaService mdmsSchemaService;


    public String addVcSchema(String mdmsSchema) throws JsonProcessingException {
        // Convert the received JSON string to JSON object
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            credentialSchemaPayload = objectMapper.readValue(mdmsSchema, JsonNode.class);
            vcSchemaPayload = credentialSchemaPayload.deepCopy();
            vcSchemaPayload.remove("mdmsData");
        }
        catch (IOException e) {
            throw new RuntimeException("mdmsData field required in the request payload" + e.getMessage());
        }

        // Create a new HttpHeaders object
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        System.out.println("vc schema payload"+ vcSchemaPayload);
        System.out.println("cred payload"+ credentialSchemaPayload);

        // Create a new HttpEntity with the modified payload and headers
        HttpEntity<Object> requestEntity = new HttpEntity<>(vcSchemaPayload, headers);
        // Make a POST request to the schema endpoint
        ResponseEntity<String> responseEntity = new RestTemplate().exchange(SCHEMA_ENDPOINT, HttpMethod.POST, requestEntity, String.class);
        String addSchemaResponse=responseEntity.getBody();
        String schemaId=getSchemaIdFromResponse(addSchemaResponse);
        // Print the response received from the request
        System.out.println("Response from schema endpoint and schema id : " + responseEntity.getBody());
        System.out.println("schema id : " + schemaId);

        System.out.println(addVcSchemaToMdms(credentialSchemaPayload,schemaId));
        return responseEntity.getBody();
    }


    public String getSchemaIdFromResponse(String addSchemaResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        String schemaId;
        try {
            JsonNode responseBody = objectMapper.readTree(addSchemaResponse);
            schemaId = responseBody.path("schema").path("id").asText();
        } catch (IOException e) {
            // Handle parsing errors by providing a more informative error message
            throw new RuntimeException("no Id in the response   " + e.getMessage());
        }
        return schemaId;
    }

    public String addVcSchemaToMdms(JsonNode mdmsDataRequestPayload,String schemaId) throws JsonProcessingException {
            // Initialize ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            RestTemplate restTemplate = new RestTemplate();

            System.out.println("mdms payload redceieved is "+mdmsDataRequestPayload);
            // Create the payload
            String uuid = mdmsDataRequestPayload.path("mdmsData").path("uuid").asText();
            JsonNode path = mdmsDataRequestPayload.path("mdmsData").path("path");
            String did = mdmsDataRequestPayload.path("schema").path("author").asText();
            String mdmsCodeName=mdmsDataRequestPayload.path("mdmsData").path("code").asText();




            System.out.println("uuid path did and mdmsCodeName is "+uuid);
            System.out.println("uuid path did and mdmsCodeName is "+path);
            System.out.println("uuid path did and mdmsCodeName is "+did);
            System.out.println("uuid path did and mdmsCodeName is "+mdmsCodeName);


            String mdmsSchemaPayload = "{\"RequestInfo\":{\"apiId\":\"Rainmaker\",\"authToken\":\"8c68a385-196a-4790-8aee-42323faef9ad\",\"userInfo\":{\"id\":595,\"uuid\":\"1fda5623-448a-4a59-ad17-657986742d67\",\"userName\":\"UNIFIED_DEV_USERR\",\"name\":\"Unified dev user\",\"mobileNumber\":\"8788788851\",\"emailId\":\"\",\"locale\":null,\"type\":\"EMPLOYEE\",\"roles\":[{\"name\":\"Localisation admin\",\"code\":\"LOC_ADMIN\",\"tenantId\":\"pg\"},{\"name\":\"Employee\",\"code\":\"EMPLOYEE\",\"tenantId\":\"pg\"},{\"name\":\"MDMS Admin\",\"code\":\"MDMS_ADMIN\",\"tenantId\":\"pg\"},{\"name\":\"SUPER USER\",\"code\":\"SUPERUSER\",\"tenantId\":\"pg\"}],\"active\":true,\"tenantId\":\"pg\",\"permanentCity\":null},\"msgId\":\"1695889012604|en_IN\",\"plainAccessRequest\":{}},\"SchemaDefinition\":{\"tenantId\":\"default\",\"code\":\"VerifiableCredentials.test2\",\"description\":\"Trade type for trade license applications\",\"definition\":{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"type\":\"object\",\"required\":[\"path\"],\"x-unique\":[\"path\"]},\"isActive\":true}}";
            JsonNode mdmsSchemaPayloadObject = objectMapper.readTree(mdmsSchemaPayload);

            ObjectNode schemaDefinitionNode = (ObjectNode) mdmsSchemaPayloadObject.path("SchemaDefinition");
            schemaDefinitionNode.put("code",mdmsCodeName);

            // Add uuid, did, and path to the definition node
            ObjectNode definitionNode = (ObjectNode) mdmsSchemaPayloadObject.path("SchemaDefinition").path("definition");
            definitionNode.put("schemaId",schemaId);
            definitionNode.put("uuid", uuid);
            definitionNode.put("did", did);
            definitionNode.set("path", path);

            System.out.println("mdms payload objects is "+ mdmsSchemaPayloadObject);
            // Define the URL

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Set up the request entity
            HttpEntity<JsonNode> requestEntity = new HttpEntity<>(mdmsSchemaPayloadObject, headers);

            // Make the POST request
            ResponseEntity<String> responseEntity = restTemplate.exchange(mdmsRequestUrl, HttpMethod.POST, requestEntity, String.class);

            // Print the response
            System.out.println("mdms v2 Response status code: " + responseEntity.getStatusCode());
            System.out.println("mdms v2 Response body: " + responseEntity.getBody());

            mdmsSchemaService.invalidateMdmsCache("vc-mdms");
            mdmsSchemaService.loadSchemaFromMdms();
            String mdmsResponse= responseEntity.getBody();


            return mdmsResponse;
    }
}
