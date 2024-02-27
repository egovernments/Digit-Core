package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.egov.sunbirdrc.models.CredentialPayloadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Component
public class MdmsSchemaService {

    @Autowired
    private CredentialPayloadRequest credentialPayloadRequest;

    @Value("${sunbird.mdms.schema.url}")
    private String getSchemaUrl;

    @Value("${sunbird.mdms.auth.token}")
    private String mdmsToken;

    private StringRedisTemplate stringRedisTemplate;
    public MdmsSchemaService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    //method to load the relevant credential schema into the memory from mdms on start of the service
    @PostConstruct
    public void loadSchemaFromMdms() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", mdmsToken); // Set if required

        String requestJson = """
                {
                    "RequestInfo": {
                        "apiId": "asset-services",
                        "ver": null,
                        "ts": null,
                        "action": null,
                        "did": null,
                        "key": null,
                        "msgId": "search with from and to values",
                        "authToken": "8c68a385-196a-4790-8aee-42323faef9ad",
                        "correlationId": null,
                        "userInfo": {
                            "id": 595,
                            "uuid": "1fda5623-448a-4a59-ad17-657986742d67",
                            "userName": "UNIFIED_DEV_USERR",
                            "name": "Unified dev user",
                            "mobileNumber": "8788788851",
                            "emailId": "",
                            "locale": null,
                            "type": "EMPLOYEE",
                            "roles": [
                                {
                                    "name": "Localisation admin",
                                    "code": "LOC_ADMIN",
                                    "tenantId": "pg"
                                },
                                {
                                    "name": "Employee",
                                    "code": "EMPLOYEE",
                                    "tenantId": "pg"
                                },
                                {
                                    "name": "MDMS Admin",
                                    "code": "MDMS_ADMIN",
                                    "tenantId": "pg"
                                },
                                {
                                    "name": "SUPER USER",
                                    "code": "SUPERUSER",
                                    "tenantId": "pg"
                                }
                            ],
                            "active": true,
                            "tenantId": "pg",
                            "permanentCity": null
                        },
                        "msgId": "1695889012604|en_IN",
                        "plainAccessRequest": {}
                    },
                    "SchemaDefCriteria": {
                        "tenantId": "default",
                        "code": ["VerifiableCredentials.tradelicense"]
                    }
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        String response = restTemplate.postForObject(getSchemaUrl, entity, String.class);
        System.out.println("Response from MDMS schema: " + response);
        stringRedisTemplate.opsForValue().set("tradelicense-mdms", response);
    }



    public CredentialPayloadRequest getJsonPathMdmsSchema(String key) {
        // Retrieve the JSON string from Redis
        String mdmsJsonResponse= stringRedisTemplate.opsForValue().get(key);
        return getJsonPathFromResponse(mdmsJsonResponse);
    }


    //jsonPath from the response to extract relevant fields for payload creation
    private CredentialPayloadRequest getJsonPathFromResponse(String mdmsJsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(mdmsJsonResponse);
            JsonNode schemaDefinitions = rootNode.path("SchemaDefinitions");
            if (schemaDefinitions.isArray() && !schemaDefinitions.isEmpty()) {
                JsonNode properties = schemaDefinitions.get(0).path("definition").path("properties");

                // Dynamically extract jsonpath for each property
                properties.fields().forEachRemaining(field -> {
                    String fieldName = field.getKey();
                    JsonNode fieldValue = field.getValue().path("jsonpath");
                    String jsonPath = fieldValue.asText();

                    // Dynamically set the jsonpath to the corresponding field in CredentialPayloadResponse
                    switch (fieldName) {
                        case "id":
                            credentialPayloadRequest.setId(jsonPath);
                            break;
                        case "licenseNumber":
                            credentialPayloadRequest.setLicenseNumber(jsonPath);
                            break;
                        case "licenseType":
                            credentialPayloadRequest.setLicenseType(jsonPath);
                            break;
                        case "tradeName":
                            credentialPayloadRequest.setTradeName(jsonPath);
                            break;
                        case "applicationNumber":
                            credentialPayloadRequest.setApplicationNumber(jsonPath);
                            break;
                        // Add more cases as needed for additional fields
                    }
                });

                // Display extracted jsonpaths
                System.out.println("ID JsonPath: " + credentialPayloadRequest.getId());
                System.out.println("Trade Name JsonPath: " + credentialPayloadRequest.getTradeName());
                System.out.println("License Type JsonPath: " + credentialPayloadRequest.getLicenseType());
                System.out.println("License Number JsonPath: " + credentialPayloadRequest.getLicenseNumber());
                System.out.println("Application Number JsonPath: " + credentialPayloadRequest.getApplicationNumber());

            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return credentialPayloadRequest;
    }
}
