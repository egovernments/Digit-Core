package org.egov.sunbirdrc.service;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Component
public class MdmsSchemaService {

    private StringRedisTemplate stringRedisTemplate;
    public MdmsSchemaService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void loadSchemaFromMdms() {
        String url = "http://localhost:9002/mdms-v2/schema/v1/_search";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer 8c68a385-196a-4790-8aee-42323faef9ad"); // Set if required

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
                        "code": "VerifiableCredentials.tradelicense"
                    }
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        String response = restTemplate.postForObject(url, entity, String.class);
        System.out.println("Response from MDMS schema: " + response);
        stringRedisTemplate.opsForValue().set("tradelicense-mdms", response);
    }


    public String retrieveJsonObject(String key) {
        // Retrieve the JSON string from Redis
        return stringRedisTemplate.opsForValue().get("tradelicense-mdms");
    }
}
