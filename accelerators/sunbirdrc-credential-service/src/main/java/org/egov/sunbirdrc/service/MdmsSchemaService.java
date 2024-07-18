package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.sunbirdrc.models.MdmsSearch;
import org.egov.sunbirdrc.models.SchemaDefCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Component
@Slf4j
public class MdmsSchemaService {


    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search}")
    private String mdmsSearchUrl;

    @Value("${egov.mdms.search.tenant}")
    private String mdmsSearchTenantId;


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
        RequestInfo requestInfo= new RequestInfo();
        SchemaDefCriteria schemaDefinitionSearch= SchemaDefCriteria.builder().
                tenantId(mdmsSearchTenantId)
                .build();

        MdmsSearch mdmsSearchObject= MdmsSearch.builder()
                .requestInfo(requestInfo)
                .schemaDefCriteria(schemaDefinitionSearch)
                .build();

        String mdmsSearchObjectString=null;
        try {
            mdmsSearchObjectString = objectMapper.writeValueAsString(mdmsSearchObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> entity = new HttpEntity<>(mdmsSearchObjectString, headers);
        StringBuilder getSchemaUrl= new StringBuilder();
        getSchemaUrl.append(mdmsHost).append(mdmsSearchUrl);
        //getSchemaUrl.append("http://localhost:9002/mdms-v2/schema/v1/_search");
        String mdmsSearchResponse=null;
        try{
            mdmsSearchResponse = restTemplate.postForObject(getSchemaUrl.toString(), entity, String.class);

        }
        catch(Exception e){
            throw new CustomException("MDMS_SEARCH_FAILURE", "failed to fetch details from mdms");
        }

        stringRedisTemplate.opsForValue().set("vc-mdms", mdmsSearchResponse);

    }


    public Boolean invalidateMdmsCache(String key){
        return stringRedisTemplate.delete(key);
    }

    public JsonNode getModuleDetailsFromMdmsData(String entityModuleName) {
        loadSchemaFromMdms();
        try {
            String response = stringRedisTemplate.opsForValue().get("vc-mdms");
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
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
        } catch(Exception e){
            throw new CustomException("MDMS_PARSING_ERROR", "error parsing the mdms data");
        }
        return null;
    }
}