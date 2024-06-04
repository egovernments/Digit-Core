package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.sunbirdrc.models.MdmsSearch;
import org.egov.sunbirdrc.models.Schema;
import org.egov.sunbirdrc.models.SchemaDefinitionSearch;
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
@Slf4j
public class MdmsSchemaService {

    @Value("${sunbird.mdms.auth.token}")
    private String mdmsToken;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search}")
    private String mdmsSearchUrl;

    @Autowired
    private ObjectMapper objectMapper;
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RestTemplate restTemplate;



    public MdmsSchemaService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    //load the rc config in the mdms to the cache
    @PostConstruct
    public void loadSchemaFromMdms() {
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
                        "tenantId": "default"
                    }
                }
                """;

        RequestInfo requestInfo= new RequestInfo();
        SchemaDefinitionSearch  schemaDefinitionSearch= SchemaDefinitionSearch.builder().
            tenantId("default")
            .build();

        MdmsSearch mdmsSearchObject= MdmsSearch.builder().
                requestInfo(requestInfo)
                .schemaDefinitionSearch(schemaDefinitionSearch)
                .build();


        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        StringBuilder getSchemaUrl= new StringBuilder();
        getSchemaUrl.append(mdmsHost).append(mdmsSearchUrl);
        //getSchemaUrl.append("http://localhost:9002/mdms-v2/schema/v1/_search");
        String response = restTemplate.postForObject(getSchemaUrl.toString(), entity, String.class);
        stringRedisTemplate.opsForValue().set("vc-mdms", response);

    }


    public Boolean invalidateMdmsCache(String key){
        return stringRedisTemplate.delete(key);
    }

    public JsonNode getModuleDetailsFromMdmsData(String entityModuleName) {
        try {
            String response = stringRedisTemplate.opsForValue().get("vc-mdms");
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                // Access the SchemaDefinitions array within the response
                JsonNode schemaDefinitions = rootNode.path("SchemaDefinitions");
                if (schemaDefinitions.isArray()) {
                    for (JsonNode definitionNode : schemaDefinitions) {
                        JsonNode keyNode = definitionNode.get("code");
                        if (keyNode != null && entityModuleName.equals(keyNode.asText())) {
                            return definitionNode;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while parsing MDMS data: " + e.getMessage());
        }
        return null;
    }
}
