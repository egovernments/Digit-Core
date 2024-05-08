package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.unix.Errors;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.models.MdmsData;
import org.egov.sunbirdrc.models.MdmsSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

@Service
@Slf4j
public class AddVcSchemaService {

    @Value("${sunbird.mdms.create.url}")
    private String mdmsRequestUrl;

    @Value("${sunbird.credential.schema.host}")
    private String credentialHost;

    @Value("${sunbird.credential.schema.path}")
    private String credentialPath;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    private JsonNode mdmsDataRequestPayload;
    private JsonNode credentialSchemaPayload;
    private ObjectNode vcSchemaPayload;

    @Autowired
    private MdmsSchemaService mdmsSchemaService;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    public Object addVcSchema(MdmsSchema mdmsSchema) throws JsonProcessingException {
        // Convert the received JSON string to JSON object
        try {
            String mdmsJsonPayload = objectMapper.writeValueAsString(mdmsSchema);
            log.info("mdms payload is" + mdmsJsonPayload);
            credentialSchemaPayload = objectMapper.readValue(mdmsJsonPayload, JsonNode.class);
            vcSchemaPayload = credentialSchemaPayload.deepCopy();
            vcSchemaPayload.remove("mdmsData");
        }
        catch (IOException e) {
            throw new RuntimeException("mdmsData field required in the request payload" + e.getMessage());
        }

        // Create a new HttpHeaders object
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create a new HttpEntity with the modified payload and headers
        HttpEntity<Object> requestEntity = new HttpEntity<>(vcSchemaPayload, headers);
        // Make a POST request to the schema endpoint
        log.info("request entity"+ requestEntity);
        StringBuilder uri = new StringBuilder();
        uri.append(credentialHost).append(credentialPath);
        log.info("Constructed URI: {}", uri.toString());
        Object response = serviceRequestRepository.fetchResult(uri, requestEntity).toString();
        //ResponseEntity<String> responseEntity = restTemplate.exchange(SCHEMA_ENDPOINT, HttpMethod.POST, requestEntity, String.class);
        //String addSchemaResponse=response.getBody();
        String schemaId=getSchemaIdFromResponse(response);
        String mdmsResponse=addVcSchemaToMdms(credentialSchemaPayload,schemaId);
        log.info("mdms response on adding the schema"+ mdmsResponse);
        //ObjectNode objectNode = objectMapper.valueToTree(response);

        return response;
    }


    public String getSchemaIdFromResponse(Object addSchemaResponse) {
        String schemaId;
        JsonNode responseBody = objectMapper.valueToTree(addSchemaResponse);
        schemaId = responseBody.path("schema").path("id").asText();
        return schemaId;
    }

    public String addVcSchemaToMdms(JsonNode mdmsDataRequestPayload,String schemaId) throws JsonProcessingException {
        // Create the payload
        String uuid = mdmsDataRequestPayload.path("mdmsData").path("uuid").asText();
        JsonNode path = mdmsDataRequestPayload.path("mdmsData").path("path");
        String did = mdmsDataRequestPayload.path("schema").path("author").asText();
        String mdmsCodeName=mdmsDataRequestPayload.path("mdmsData").path("code").asText();

        String mdmsSchemaPayload = "{\"RequestInfo\":{\"apiId\":\"Rainmaker\",\"authToken\":\"256596c3-6bed-4b2f-8dcb-18bb0c1f2fad\",\"userInfo\":{\"id\":595,\"uuid\":\"1fda5623-448a-4a59-ad17-657986742d67\",\"userName\":\"UNIFIED_DEV_USERR\",\"name\":\"Unified dev user\",\"mobileNumber\":\"8788788851\",\"emailId\":\"\",\"locale\":null,\"type\":\"EMPLOYEE\",\"roles\":[{\"name\":\"Localisation admin\",\"code\":\"LOC_ADMIN\",\"tenantId\":\"pg\"},{\"name\":\"Employee\",\"code\":\"EMPLOYEE\",\"tenantId\":\"pg\"},{\"name\":\"MDMS Admin\",\"code\":\"MDMS_ADMIN\",\"tenantId\":\"pg\"},{\"name\":\"SUPER USER\",\"code\":\"SUPERUSER\",\"tenantId\":\"pg\"}],\"active\":true,\"tenantId\":\"pg\",\"permanentCity\":null},\"msgId\":\"1695889012604|en_IN\",\"plainAccessRequest\":{}},\"SchemaDefinition\":{\"tenantId\":\"default\",\"code\":\"VerifiableCredentials.test2\",\"description\":\"Trade type for trade license applications\",\"definition\":{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"type\":\"object\",\"required\":[\"path\"],\"x-unique\":[\"path\"]},\"isActive\":true}}";
        JsonNode mdmsSchemaPayloadObject = objectMapper.readTree(mdmsSchemaPayload);

        ObjectNode schemaDefinitionNode = (ObjectNode) mdmsSchemaPayloadObject.path("SchemaDefinition");
        schemaDefinitionNode.put("code",mdmsCodeName);

        // Add uuid, did, and path to the definition node
        ObjectNode definitionNode = (ObjectNode) mdmsSchemaPayloadObject.path("SchemaDefinition").path("definition");
        definitionNode.put("schemaId",schemaId);
        definitionNode.put("uuid", uuid);
        definitionNode.put("did", did);
        definitionNode.set("path", path);

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Set up the request entity
        HttpEntity<JsonNode> requestEntity = new HttpEntity<>(mdmsSchemaPayloadObject, headers);

        // Make the POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(mdmsRequestUrl, HttpMethod.POST, requestEntity, String.class);

        mdmsSchemaService.invalidateMdmsCache("vc-mdms");
        mdmsSchemaService.loadSchemaFromMdms();
        String mdmsResponse= responseEntity.getBody();

        return mdmsResponse;
    }
}