package org.egov.handler.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.Mdms;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class ElasticsearchUtil {

    public static final String ES_INDEX_HEADER_FORMAT = "{\"index\":{\"_index\":\"%s\",\"_id\":\"%s\"}}\n";
    public static final String ERRORS_PATH = "$.errors";

    private ServiceConfiguration config;

    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    private ResourceLoader resourceLoader;


    public ElasticsearchUtil(ServiceConfiguration config, RestTemplate restTemplate, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    public String getESEncodedCredentials() {
        String credentials = config.getEsUsername() + ":" + config.getEsPassword();
        byte[] credentialsBytes = credentials.getBytes();
        byte[] base64CredentialsBytes = Base64.getEncoder().encode(credentialsBytes);
        return "Basic " + new String(base64CredentialsBytes);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getESEncodedCredentials());
        return headers;

    }

    public void createDefaultRecords(String tenantId) {

        Resource resource = resourceLoader.getResource("classpath:PgrIndexer.json");

        StringBuilder bulkRequest = new StringBuilder();

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String module = field.getKey();
                JsonNode moduleNode = field.getValue();

                if (moduleNode.isArray()) {
                    for (JsonNode itemNode : moduleNode) {
                        ((ObjectNode) itemNode.get("Data").get("service")).put("tenantId", tenantId);
                        String req = processJsonObject("pgr-services", itemNode);
                        bulkRequest.append(req);
                        bulkRequest.append(itemNode.toString() + "\n");
                    }
                }
            }
            String uri = config.getElasticsearchHost() + config.getBulkIndexPath();
            esPost(uri, bulkRequest.toString());
        } catch (IOException e) {
            throw new CustomException("IO_EXCEPTION", "Error reading or mapping JSON file: " + e.getMessage());
        }

    }

    public void esPost(String uri, String request) {
        try {
            log.debug("Record being indexed: {}", request);

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.add("Authorization", getESEncodedCredentials());
            final HttpEntity<String> entity = new HttpEntity<>(request, headers);

            String response = restTemplate.postForObject(uri, entity, String.class);

            if (uri.contains("_bulk") && JsonPath.read(response, ERRORS_PATH).equals(true)) {
                log.info("Indexing FAILED!!!!");
                log.info("Response from ES: {}", response);
            }
        } catch (final ResourceAccessException e) {
            log.error("ES is DOWN, Pausing kafka listener.......");
        } catch (Exception e) {
            log.error("Exception while trying to index the ES documents. Note: ES is not Down.", e);
        }
    }

    public String processJsonObject(String indexName, JsonNode data) {

        return String.format(ES_INDEX_HEADER_FORMAT, indexName, UUID.randomUUID().toString(), data.toString());
    }


}
