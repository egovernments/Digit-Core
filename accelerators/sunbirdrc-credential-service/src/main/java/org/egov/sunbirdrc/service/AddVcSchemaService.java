package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.sunbirdrc.models.DigitMDMSRequestBody;
import org.egov.sunbirdrc.models.MdmsSchema;
import org.egov.sunbirdrc.models.SchemaDefinition;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class AddVcSchemaService {

    @Value("${egov.mdms.create}")
    private String mdmsRequestUrl;

    @Value("${sunbird.credential.schema.host}")
    private String credentialHost;

    @Value("${sunbird.credential.schema.path}")
    private String credentialPath;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.create}")
    private String mdmsCreateUrl;

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
        uri.append(credentialHost).append("/credential-schema");
        //uri.append("https://unified-dev.digit.org/credential-schema-service/credential-schema");
        log.info("Constructed URI: {}", uri.toString());
        Object response = serviceRequestRepository.fetchResult(uri, requestEntity);
        //ResponseEntity<String> responseEntity = restTemplate.exchange(SCHEMA_ENDPOINT, HttpMethod.POST, requestEntity, String.class);
        //String addSchemaResponse=response.getBody();
        String schemaId=getSchemaIdFromResponse(response);
        String mdmsResponse=addVcSchemaToMdms(credentialSchemaPayload,schemaId);
        log.info("mdms response on adding the schema"+ mdmsResponse);
        return response;
    }



    public String getSchemaIdFromResponse(Object addSchemaResponse) throws JsonProcessingException{
        System.out.println(objectMapper.writeValueAsString(addSchemaResponse).toString());
        String schemaId=null;
        try{
            schemaId = JsonPath.read(addSchemaResponse, "$.schema.id");
        }
        catch(Exception e){
            throw new CustomException("ID_NOT_FOUND", "id not found in the schema");
        }
        return schemaId;
    }

    public String addVcSchemaToMdms(JsonNode mdmsDataRequestPayload,String schemaId) throws JsonProcessingException {

        List<String> requiredList = Arrays.asList("path");
        List<String> uniqueList = Arrays.asList("path");

        // Create ArrayNode for requiredList
        ArrayNode requiredArray = objectMapper.valueToTree(requiredList);

        // Create ArrayNode for uniqueList
        ArrayNode uniqueArray = objectMapper.valueToTree(uniqueList);

        // Create the payload
        String uuid = mdmsDataRequestPayload.path("mdmsData").path("uuid").asText();
        JsonNode path = mdmsDataRequestPayload.path("mdmsData").path("path");
        String did = mdmsDataRequestPayload.path("schema").path("author").asText();
        String mdmsCodeName=mdmsDataRequestPayload.path("mdmsData").path("code").asText();
        JsonNode mdmsRcContext=mdmsDataRequestPayload.path("mdmsData").path("context");

        RequestInfo requestInfo = objectMapper.convertValue(mdmsDataRequestPayload.path("RequestInfo"),RequestInfo.class);

        SchemaDefinition schemaDefinition = SchemaDefinition.builder().
                tenantId("default")
                .code(mdmsCodeName)
                .description("Trade type for trade license applications")
                .isActive(Boolean.TRUE)
                .definition(objectMapper.createObjectNode())
                .build();

        // Add uuid, did, and path to the definition node
        ObjectNode definitionNode = (ObjectNode) schemaDefinition.getDefinition();
        definitionNode.put("schemaId",schemaId);
        definitionNode.put("uuid", uuid);
        definitionNode.put("did", did);
        definitionNode.put("context",mdmsRcContext);
        definitionNode.put("required",requiredArray);
        definitionNode.put("x-unique", uniqueArray);
        definitionNode.set("path", path);

        DigitMDMSRequestBody mdmsRequestBody = DigitMDMSRequestBody.builder()
                .requestInfo(requestInfo)
                .SchemaDefinition(schemaDefinition)
                .build();

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Set up the request entity
        HttpEntity<DigitMDMSRequestBody> requestEntity = new HttpEntity<>(mdmsRequestBody, headers);

        StringBuilder mdmsRequestUrl= new StringBuilder();
        mdmsRequestUrl.append(mdmsHost).append(mdmsCreateUrl);
        // Make the POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(mdmsRequestUrl.toString(), HttpMethod.POST, requestEntity, String.class);

        mdmsSchemaService.invalidateMdmsCache("vc-mdms");
        mdmsSchemaService.loadSchemaFromMdms();
        String mdmsResponse= responseEntity.getBody();

        return mdmsResponse;
    }
}