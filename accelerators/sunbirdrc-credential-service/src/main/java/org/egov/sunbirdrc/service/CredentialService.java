package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.kafka.Producer;
import org.egov.sunbirdrc.models.CredentialIdResponse;
import org.egov.sunbirdrc.models.CredentialIdUuidMapper;
import org.egov.sunbirdrc.models.CredentialRequest;
import org.egov.sunbirdrc.models.QrCodeRequest;
import org.egov.sunbirdrc.repository.CredentialUuidRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;


@Service
@Slf4j
@Getter
@Setter
public class CredentialService {


    @Value("${sunbird.save.vc.topic}")
    private String saveVcidTopic;

    @Value("${sunbird.update.vc.topic}")
    private String updateVcidTopic;

    @Value("${sunbird.credential.host}")
    private String credentialHost;

    @Value("${sunbird.credential.path}")
    private String credentialPath;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

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

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CredentialRequest credentialRequest;



    public void processPayloadAndPersistCredential(String entityRequestPayload,String topic) {
        try{

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode requestPayload = objectMapper.readTree(entityRequestPayload);
            String entityModuleName = requestPayload.path("module").asText();

            JsonNode mdmsModuleObject = mdmsSchemaService.getModuleDetailsFromMdmsData(entityModuleName);

            //get did, schemaId, uuid and jsonpath details from the mdms object
            String entityDid= getDidFromModuleObject(mdmsModuleObject);
            String entitySchemaId=getSchemaIdFromModuleObject(mdmsModuleObject);
            String uuidJsonPath=getUuidFromModuleObject(mdmsModuleObject); // use the jsonpath uuid obtained here to extract the actual id from requestPayload, convert requestpaylod to string first.
            String uuid=JsonPath.read(entityRequestPayload, uuidJsonPath);
            String expiryDate= getExpiryDateFromModuleObject(mdmsModuleObject);
            JsonNode listofJsonPaths= getAllFieldPathFromMdms(mdmsModuleObject);
            JsonNode credentialContext=getContextFromMdms(mdmsModuleObject);

            JsonNode payloadFromJsonPath= extractPayloadFromJsonPath(listofJsonPaths,requestPayload,entityDid);
            //check condition for create-vc and recreate-vc
            if (topic.equals("recreate-vc")){
                CredentialIdUuidMapper credentialUuidObject=credentialUuidRepository.getUuidVcidMapperRow(uuid);
                String revokeApiResponse=revokeCredentialService.revokeCredential(credentialUuidObject.getVcid());
                JsonNode jsonNode = objectMapper.readTree(revokeApiResponse);
                String status = jsonNode.get("status").asText();
                if(status.equals("REVOKED")){
                    String credentialIdUuidData=generateCredentials(uuid, entityDid, entitySchemaId,payloadFromJsonPath,credentialContext,expiryDate);
                    if(credentialIdUuidData!=null){
                        producer.push(updateVcidTopic, credentialIdUuidData);
                    }
                }
                else{
                    log.error("verifiable credentials for the entity is not revoked");
                }
            }
            else{
                String credentialIdUuidData=generateCredentials(uuid, entityDid, entitySchemaId,payloadFromJsonPath,credentialContext,expiryDate);
                if (credentialIdUuidData!=null){
                    producer.push("save-vcid", credentialIdUuidData);
                }
            }
            //update cache after db update
            credentialUuidRepository.invalidateCache("vcid_uuid_mapper","vc-mdms");
            credentialUuidRepository.loadData();
        }catch (JsonProcessingException e) {
            log.error("Exception occurred while processing JSON: " + e.getMessage());
        }
    }

    public String getDidFromModuleObject(JsonNode mdmsModuleObject) {
        return getFieldDetails(mdmsModuleObject, "did");
    }

    public String getExpiryDateFromModuleObject(JsonNode mdmsModuleObject) {
        return getFieldDetails(mdmsModuleObject,"expiryDate");
    }

    public String getSchemaIdFromModuleObject(JsonNode mdmsModuleObject) {
        return getFieldDetails(mdmsModuleObject, "schemaId");
    }

    public JsonNode getContextFromMdms(JsonNode mdmsModuleObject){
        return getContextObject(mdmsModuleObject, "context");
    }

    public String getUuidFromModuleObject(JsonNode mdmsModuleObject) {
        return getFieldDetails(mdmsModuleObject, "uuid");
    }



    public String getFieldDetails(JsonNode mdmsModuleObject, String fieldName) {
        if (mdmsModuleObject != null && mdmsModuleObject.has("definition")) {
            JsonNode definitionNode = mdmsModuleObject.get("definition");
            if (definitionNode != null && definitionNode.has(fieldName)) {
                return definitionNode.get(fieldName).asText();
            }
        }
        return null;
    }

    public JsonNode getContextObject(JsonNode mdmsModuleObject, String fieldName){
        if (mdmsModuleObject != null && mdmsModuleObject.has("definition")) {
            JsonNode definitionNode = mdmsModuleObject.get("definition");
            if (definitionNode != null && definitionNode.has(fieldName)) {
                return definitionNode.get(fieldName);
            }
        }
        return null;
    }

    public JsonNode getAllFieldPathFromMdms(JsonNode mdmsModuleObject){
        if (mdmsModuleObject != null && mdmsModuleObject.has("definition")) {
            JsonNode definitionNode = mdmsModuleObject.get("definition");
            if (definitionNode != null && definitionNode.has("path")) {
                JsonNode fieldJsonPathList = definitionNode.get("path");
                return fieldJsonPathList;
            }
        }
        return null;
    }


    public JsonNode extractPayloadFromJsonPath(JsonNode listofJsonPaths, JsonNode requestPayload,String did){
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode credentialPayloadData = objectMapper.createObjectNode();

        listofJsonPaths.forEach(pathNode -> {
            try {
                String lastKey = pathNode.fieldNames().next();
                String fieldPath = pathNode.get(lastKey).asText();
                Object fieldValue = JsonPath.read(requestPayload.toString(), fieldPath);
                if (fieldValue != null) {
                    credentialPayloadData.put(lastKey, fieldValue.toString());
                } else {
                    credentialPayloadData.putNull(lastKey);
                }
            } catch (PathNotFoundException e) {
                // Path not found in requestPayload
                log.error("json path not found in the payload",e);
            }
        });
        //adding id field which is mandatory and can be random id according to sunbird api
        credentialPayloadData.put("id", did);
        return credentialPayloadData;
    }


    public String generateCredentials(String uuid, String did, String schemaId, JsonNode credentialPayload,JsonNode credentialContext,String expiryDate) {
        if (did == null || schemaId == null || uuid == null) {
            throw new IllegalArgumentException("Did, schemaId, and uuid cannot be null");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // Create a new instance of CredentialRequestBody
            credentialRequest.setContext(Arrays.asList("https://www.w3.org/2018/credentials/v1", credentialContext));
            credentialRequest.setId(did);
            credentialRequest.setType(Arrays.asList("VerifiableCredential"));
            credentialRequest.setIssuer(did);
            credentialRequest.setExpirationDate(expiryDate);
            credentialRequest.setCredentialSubject(credentialPayload);

            // Convert the credential part to JSON
            String credentialRequestPayload = objectMapper.writeValueAsString(credentialRequest);

            // Define the request body
            String requestBody = "{\n" +
                    "    \"credential\": " + credentialRequestPayload + ",\n" +
                    "    \"credentialSchemaId\": \"" + schemaId + "\",\n" +
                    "    \"credentialSchemaVersion\": \"1.0.0\"\n" +
                    "}";

            // Create the request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            // Make the HTTP POST request
            StringBuilder fetchCredentialUrl = new StringBuilder();
            fetchCredentialUrl.append(credentialHost).append(credentialPath);
            //fetchCredentialUrl.append("https://unified-dev.digit.org/credentials-service/credentials/issue");
            Object credentialResponse = serviceRequestRepository.fetchResult(fetchCredentialUrl, requestEntity);
            credentialIdUuidMapper.setVcid(getIdFromResponse(credentialResponse));
            credentialIdUuidMapper.setEntityid(uuid);
            credentialIdUuidMapper.setCreatedBy(did);
            return objectMapper.writeValueAsString(credentialIdUuidMapper);
        } catch (JsonProcessingException e) {
            log.error("Exception occurred while processing JSON: " + e.getMessage());
            return null;
        }
    }

    public CredentialIdResponse getCredential(QrCodeRequest qrCodeRequest)  {
        JsonNode schemaObject=mdmsSchemaService.getModuleDetailsFromMdmsData(qrCodeRequest.getCode());

        String schemaId=null;
        try{String schemaObjectResponse=objectMapper.writeValueAsString(schemaObject);
            schemaId = JsonPath.read(schemaObjectResponse, "$.definition.schemaId");
        }
        catch(Exception e){
            throw new CustomException("ID_NOT_FOUND", "credential id not found in the schema");
        }
        CredentialIdUuidMapper credentialUuidObject=credentialUuidRepository.getUuidVcidMapperRow(qrCodeRequest.getUuid());
        CredentialIdResponse credentialIdResponse= new CredentialIdResponse();
        credentialIdResponse.setCredentialId(credentialUuidObject.getVcid());
        credentialIdResponse.setSchemaId(schemaId);
        credentialIdResponse.setResponseInfo(null);
        return credentialIdResponse;
    }


    private String getIdFromResponse(Object credentialResponse) throws JsonProcessingException {
        String credentialId=null;
        try{
            credentialId = JsonPath.read(credentialResponse, "$.credential.id");
            log.info("credential is generated, id is"+credentialId);

        }
        catch(Exception e){
            throw new CustomException("ID_NOT_FOUND", "credential id not found in the response");
        }
        return credentialId;
    }
}
