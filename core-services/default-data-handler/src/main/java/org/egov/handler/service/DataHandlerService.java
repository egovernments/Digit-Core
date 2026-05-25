package org.egov.handler.service;

import static org.egov.handler.config.ServiceConstants.TENANT_BOUNDARY_SCHEMA;
import static org.egov.handler.constants.UserConstants.ASSIGNER;
import static org.egov.handler.constants.UserConstants.EMPLOYEE;
import static org.egov.handler.constants.UserConstants.HRMS_CLASSPATH;
import static org.egov.handler.constants.UserConstants.RESOLVER;
import static org.egov.handler.constants.UserConstants.WELCOME_MAIL_SANDBOX_CLASSPATH;
import static org.egov.handler.constants.UserConstants.WELCOME_MAIL_SUBJECT;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.ConfigServiceUtil;
import org.egov.handler.util.HrmsUtil;
import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.util.TenantManagementUtil;
import org.egov.handler.util.WorkflowUtil;
import org.egov.handler.web.models.BusinessServiceRequest;
import org.egov.handler.web.models.DataSetupRequest;
import org.egov.handler.web.models.DefaultDataRequest;
import org.egov.handler.web.models.DefaultLocalizationDataRequest;
import org.egov.handler.web.models.DefaultMdmsDataRequest;
import org.egov.handler.web.models.Email;
import org.egov.handler.web.models.EmailRequest;
import org.egov.handler.web.models.Mdms;
import org.egov.handler.web.models.MdmsCriteriaReqV2;
import org.egov.handler.web.models.MdmsCriteriaV2;
import org.egov.handler.web.models.MdmsRequest;
import org.egov.handler.web.models.MdmsResponseV2;
import org.egov.handler.web.models.TenantConfig;
import org.egov.handler.web.models.TenantConfigRequest;
import org.egov.handler.web.models.TenantConfigResponse;
import org.egov.handler.web.models.TenantRequest;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataHandlerService {

    private final MdmsV2Util mdmsV2Util;

    private final HrmsUtil hrmsUtil;

    private final LocalizationUtil localizationUtil;

    private final TenantManagementUtil tenantManagementUtil;

    private final ConfigServiceUtil configServiceUtil;

    private final ServiceConfiguration serviceConfig;

    private final ObjectMapper objectMapper;

    private final ResourceLoader resourceLoader;

    private final WorkflowUtil workflowUtil;

    private final CustomKafkaTemplate producer;

    private final RestTemplate restTemplate;

    @Autowired
    public DataHandlerService(MdmsV2Util mdmsV2Util, HrmsUtil hrmsUtil, LocalizationUtil localizationUtil, TenantManagementUtil tenantManagementUtil, ConfigServiceUtil configServiceUtil, ServiceConfiguration serviceConfig, ObjectMapper objectMapper, ResourceLoader resourceLoader, WorkflowUtil workflowUtil, CustomKafkaTemplate producer, RestTemplate restTemplate) {
        this.mdmsV2Util = mdmsV2Util;
        this.hrmsUtil = hrmsUtil;
        this.localizationUtil = localizationUtil;
        this.tenantManagementUtil = tenantManagementUtil;
        this.configServiceUtil = configServiceUtil;
        this.serviceConfig = serviceConfig;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.workflowUtil = workflowUtil;
        this.producer = producer;
        this.restTemplate = restTemplate;
    }

    public void createDefaultData(DefaultDataRequest defaultDataRequest) {
        if (defaultDataRequest.getSchemaCodes() != null) {
            List<String> schemaCodes = new ArrayList<>(defaultDataRequest.getSchemaCodes());
			if (schemaCodes.contains(TENANT_BOUNDARY_SCHEMA)) {
				createTenantBoundarydata(defaultDataRequest.getRequestInfo(), defaultDataRequest.getTargetTenantId());
				schemaCodes.remove(TENANT_BOUNDARY_SCHEMA);
			}
            DefaultMdmsDataRequest defaultMdmsDataRequest = DefaultMdmsDataRequest.builder().requestInfo(defaultDataRequest.getRequestInfo()).targetTenantId(defaultDataRequest.getTargetTenantId()).schemaCodes(schemaCodes).onlySchemas(defaultDataRequest.getOnlySchemas()).defaultTenantId(serviceConfig.getDefaultTenantId()).build();
            mdmsV2Util.createDefaultMdmsData(defaultMdmsDataRequest);
        }
        
        try {
			createBoundaryDataFromFile(defaultDataRequest);
		} catch (IOException e) {
			e.printStackTrace();
		}

        if (defaultDataRequest.getLocales() != null && defaultDataRequest.getModules() != null) {
            for (String locale : defaultDataRequest.getLocales()) {
                DefaultLocalizationDataRequest defaultLocalizationDataRequest = DefaultLocalizationDataRequest.builder().requestInfo(defaultDataRequest.getRequestInfo()).targetTenantId(defaultDataRequest.getTargetTenantId()).locale(locale).modules(defaultDataRequest.getModules()).defaultTenantId(serviceConfig.getDefaultTenantId()).build();
                localizationUtil.createLocalizationData(defaultLocalizationDataRequest);
            }
        }

        // Copy WhatsApp notification configs from default tenant
        try {
            configServiceUtil.copyConfigData(
                    defaultDataRequest.getRequestInfo(),
                    defaultDataRequest.getTargetTenantId(),
                    serviceConfig.getDefaultConfigServiceSchemaCodes());
        } catch (Exception e) {
            log.error("Failed to copy config-service data for tenant: {}", defaultDataRequest.getTargetTenantId(), e);
        }
    }

    private void createTenantBoundarydata(RequestInfo requestInfo, String targetTenantId) {
        List<String> schemaCodes = new ArrayList<>(Collections.singletonList(TENANT_BOUNDARY_SCHEMA));

        DefaultMdmsDataRequest defaultMdmsDataRequest = DefaultMdmsDataRequest.builder().requestInfo(requestInfo).targetTenantId(targetTenantId).schemaCodes(schemaCodes).onlySchemas(Boolean.TRUE).defaultTenantId(serviceConfig.getDefaultTenantId()).build();
        mdmsV2Util.createDefaultMdmsData(defaultMdmsDataRequest);

        // Search data for the schema code in default tenetId
        List<Mdms> mdmsList = getAllMdmsResults(serviceConfig.getDefaultTenantId(), TENANT_BOUNDARY_SCHEMA, requestInfo);
        // Create schema data in the given tenantId
        for (Mdms mdms : mdmsList) {
            mdms.setTenantId(targetTenantId);

            JsonNode dataNode = mdms.getData();
            if (dataNode.has("boundary")) {
                // Cast the 'boundary' node to ObjectNode so that we can modify it
                ObjectNode boundaryNode = (ObjectNode) dataNode.get("boundary");

                // Modify the 'code' field within the 'boundary' node
                boundaryNode.put("code", targetTenantId);

                // Set the modified 'data' back to the Mdms object (optional, since it's mutable)
                ((ObjectNode) dataNode).set("boundary", boundaryNode);
                mdms.setData(dataNode);
            } else {
                log.info("Boundary node does not exist in the data.");
            }
            MdmsRequest mdmsRequest = MdmsRequest.builder().requestInfo(requestInfo).mdms(mdms).build();
            log.info("{} : {}", mdms.getSchemaCode(), mdms.getUniqueIdentifier());
            mdmsV2Util.createMdmsData(mdmsRequest);
        }
    }

    // Method to get all search results with pagination
    public List<Mdms> getAllMdmsResults(String tenantId, String schemaCode, RequestInfo requestInfo) {
        List<Mdms> allMdmsResults = new ArrayList<>();
        int limit = 100; // Default limit
        int offset = 0; // Default offset

        while (true) {
            // Create MdmsCriteriaV2 with current offset and limit
            MdmsCriteriaV2 mdmsCriteria = MdmsCriteriaV2.builder().tenantId(tenantId).schemaCode(schemaCode).offset(offset).limit(limit).build();

            MdmsCriteriaReqV2 mdmsCriteriaReq = MdmsCriteriaReqV2.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();

            // Fetch results from the repository
            MdmsResponseV2 dataSearchResponse = mdmsV2Util.searchMdmsData(mdmsCriteriaReq);
            List<Mdms> mdmsList = dataSearchResponse.getMdms();

            // Add the current batch of results to the list
            allMdmsResults.addAll(mdmsList);

            // Check if there are fewer results than the limit; if so, this is the last page
            if (mdmsList.size() < limit) {
                break;
            }

            // Update offset for the next batch
            offset += limit;
        }
        return allMdmsResults;
    }

    public void createTenantConfig(TenantRequest tenantRequest) {
        TenantConfigResponse tenantConfigSearchResponse = tenantManagementUtil.searchTenantConfig(serviceConfig.getDefaultTenantId(), tenantRequest.getRequestInfo());
        List<TenantConfig> tenantConfigList = tenantConfigSearchResponse.getTenantConfigs();

        for (TenantConfig tenantConfig : tenantConfigList) {
            // Set code and name according to target tenant
            tenantConfig.setCode(tenantRequest.getTenant().getCode());
            tenantConfig.setName(tenantRequest.getTenant().getName());

            TenantConfigRequest tenantConfigRequest = TenantConfigRequest.builder().requestInfo(tenantRequest.getRequestInfo()).tenantConfig(tenantConfig).build();

            tenantManagementUtil.createTenantConfig(tenantConfigRequest);
        }
    }

    public DefaultDataRequest setupDefaultData(DataSetupRequest dataSetupRequest) {
        DefaultDataRequest defaultDataRequest = DefaultDataRequest.builder().requestInfo(dataSetupRequest.getRequestInfo()).targetTenantId(dataSetupRequest.getTargetTenantId()).onlySchemas(dataSetupRequest.getOnlySchemas()).build();

        if (Objects.equals(dataSetupRequest.getModule(), "PGR")) {
            createPgrWorkflowConfig(dataSetupRequest.getTargetTenantId());
        }

        if (dataSetupRequest.getSchemaCodes() == null) {
            if (Objects.equals(dataSetupRequest.getModule(), "PGR")) {
                defaultDataRequest.setSchemaCodes(serviceConfig.getMdmsSchemacodeMap().get("PGR"));
            }
            if (Objects.equals(dataSetupRequest.getModule(), "HRMS")) {
                defaultDataRequest.setSchemaCodes(serviceConfig.getMdmsSchemacodeMap().get("HRMS"));
            }
        } else {
            defaultDataRequest.setSchemaCodes(dataSetupRequest.getSchemaCodes());
        }

        try {
            createDefaultData(defaultDataRequest);
        } catch (Exception e) {
            log.error("Failed to create default data for : {}", dataSetupRequest.getTargetTenantId(), e);
            throw new CustomException("DEFAULT_DATA_CREATE_FAILED", "Failed to create default data ");
        }
        return defaultDataRequest;
    }

    public void createPgrWorkflowConfig(String targetTenantId) {
        // Load the JSON file
        Resource resource = resourceLoader.getResource("classpath:PgrWorkflowConfig.json");
        try (InputStream inputStream = resource.getInputStream()) {
            BusinessServiceRequest businessServiceRequest = objectMapper.readValue(inputStream, BusinessServiceRequest.class);
            businessServiceRequest.getBusinessServices().forEach(service -> service.setTenantId(targetTenantId));
            workflowUtil.createWfConfig(businessServiceRequest);
        } catch (IOException e) {
            throw new CustomException("IO_EXCEPTION", "Error reading or mapping JSON file: " + e.getMessage());
        }
    }

    public void addMdmsData(DataSetupRequest dataSetupRequest) {

        List<Mdms> filteredMdmsData = new ArrayList<>();

        Resource resource = resourceLoader.getResource("classpath:MDMS.json");

        try (InputStream inputStream = resource.getInputStream()) {

            JsonNode rootNode = objectMapper.readTree(inputStream);
            Map<String, List<Mdms>> mdmsMap = new HashMap<>();

            // Iterate over each module (PGR, HRMS, etc.)
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String module = field.getKey();
                JsonNode moduleNode = field.getValue();

                // Convert the module node to List<Mdms>
                List<Mdms> mdmsList = new ArrayList<>();
                if (moduleNode.isArray()) {
                    for (JsonNode itemNode : moduleNode) {
                        Mdms mdms = objectMapper.treeToValue(itemNode, Mdms.class);
                        mdms.setData(itemNode);
                        mdmsList.add(mdms);
                    }
                }

                mdmsMap.put(module, mdmsList);
            }
            filteredMdmsData = mdmsMap.get(dataSetupRequest.getModule());
        } catch (IOException e) {
            throw new CustomException("IO_EXCEPTION", "Error reading or mapping JSON file: " + e.getMessage());
        }
        // Iterate over each filtered Mdms entry and create an MDMS entry
        for (Mdms mdms : filteredMdmsData) {
            mdms.setTenantId(dataSetupRequest.getTargetTenantId());
            mdms.setSchemaCode("ACCESSCONTROL-ROLEACTIONS.roleactions");
            String uniqueId = mdms.getData().get("actionid").asText() + "." + mdms.getData().get("rolecode").asText();
            mdms.setUniqueIdentifier(uniqueId);
            // Build an MdmsRequest for each entry
            MdmsRequest mdmsRequest = MdmsRequest.builder().requestInfo(dataSetupRequest.getRequestInfo()).mdms(mdms) // Assuming MdmsRequest has a field to set Mdms data
                    .build();

            // Call createMdmsData for each mdmsRequest
            mdmsV2Util.createMdmsData(mdmsRequest);
        }
    }

    public void createDefaultEmployee(String tenantId, String emailId, String employeeCode, String name) {

        Resource resource = resourceLoader.getResource(HRMS_CLASSPATH);
        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);

            // Iterate through each employee in the Employees array
            rootNode.get("Employees").forEach(employee -> {
                ((ObjectNode) employee).put("tenantId", tenantId);
                ((ObjectNode) employee).put("code", employeeCode + "@demo.com");

                // Iterate through each jurisdiction for the employee
                employee.get("jurisdictions").forEach(jurisdiction -> {
                    ((ObjectNode) jurisdiction).put("boundary", tenantId);
                    ((ObjectNode) jurisdiction).put("tenantId", tenantId);

                    // Iterate through each role for the jurisdiction
                    jurisdiction.get("roles").forEach(role -> {
                        ((ObjectNode) role).put("tenantId", tenantId);
                    });
                });

                // Update the user details for the employee
                JsonNode userNode = employee.get("user");
                ((ObjectNode) userNode).put("name", name);
                ((ObjectNode) userNode).put("tenantId", tenantId);
                ((ObjectNode) userNode).put("emailId", emailId);

                // Iterate through roles in user node
                userNode.get("roles").forEach(role -> {
                    if ((employeeCode.equals(ASSIGNER) && !role.get("code").asText().equals(EMPLOYEE))) {
                        ((ObjectNode) role).put("code", "ASSIGNER");
                        ((ObjectNode) role).put("name", "Assigner");
                        ((ObjectNode) role).put("labelKey", "ACCESSCONTROL_ROLES_ROLES_");
                    }
                    ((ObjectNode) role).put("tenantId", tenantId);
                });
            });

            String jsonPayload = objectMapper.writeValueAsString(rootNode);

            hrmsUtil.createHrmsEmployee(jsonPayload, tenantId);
        } catch (IOException e) {
            throw new CustomException("IO_EXCEPTION", "Error reading or mapping JSON file: " + e.getMessage());
        }
    }

    public void triggerWelcomeEmail(TenantRequest tenantRequest) {

        Resource resource = resourceLoader.getResource(WELCOME_MAIL_SANDBOX_CLASSPATH);
        String emailBody = "";
        try {
            emailBody = resource.getContentAsString(Charset.defaultCharset());

            // Inject values into HTML template
            emailBody = emailBody.replace("{{logInURL}}", serviceConfig.getLoginUrl());
            emailBody = emailBody.replace("{{email}}", tenantRequest.getTenant().getEmail());
            emailBody = emailBody.replace("{{orgName}}", tenantRequest.getTenant().getCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Email email = Email.builder().emailTo(Collections.singleton(tenantRequest.getTenant().getEmail())).tenantId(tenantRequest.getTenant().getCode()).isHTML(Boolean.TRUE).subject(WELCOME_MAIL_SUBJECT).body(emailBody).build();

        EmailRequest emailRequest = EmailRequest.builder().requestInfo(new RequestInfo()).email(email).build();
        producer.send(serviceConfig.getEmailTopic(), emailRequest);
    }

    public void defaultEmployeeSetup(String tenantId, String emailId) {
        createDefaultEmployee(tenantId, emailId, RESOLVER, "Rakesh Kumar");
        createDefaultEmployee(tenantId, emailId, ASSIGNER, "John Smith");
    }
    
    
    public void createBoundaryDataFromFile(DefaultDataRequest defaultDataRequest) throws IOException {

        createBoundaryDefinitionFromFile(defaultDataRequest.getRequestInfo(), defaultDataRequest.getTargetTenantId());
        createBoundaryEntityFromFile(defaultDataRequest.getRequestInfo(), defaultDataRequest.getTargetTenantId());
        // wait until boundary data is available
        waitForBoundaryData(defaultDataRequest.getTargetTenantId(), defaultDataRequest.getRequestInfo());
        createBoundaryRelationshipFromFile(defaultDataRequest.getRequestInfo(), defaultDataRequest.getTargetTenantId());
    }

    public void createBoundaryDefinitionFromFile(RequestInfo requestInfo, String targetTenantId) throws IOException {
        try{
            String hierarchyDefinitionCreateUri = serviceConfig.getHierarchyDefinitionCreateUri();

            Resource resource = resourceLoader.getResource("classpath:boundary/hierarchy-definition/hierarchy.json");
            InputStream inputStream = resource.getInputStream();

            // Read file content as raw JSON string
            String rawJson = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            rawJson = rawJson.replace("{tenantid}", targetTenantId);

            JsonNode boundaryPayload = objectMapper.readTree(rawJson);

            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", requestInfo);
            payload.put("BoundaryHierarchy", boundaryPayload.get("BoundaryHierarchy"));
            System.out.println(payload);

            restTemplate.postForObject(hierarchyDefinitionCreateUri, payload, Object.class);
            log.info("Created boundary hierarchy for tenant: {}", targetTenantId);
        }
        catch (Exception e) {
            log.error("Failed to create boundary hierarchy for tenant: {}", targetTenantId);
//            throw new CustomException("BOUNDARY_DATA_CREATE_FAILED", "Failed to create boundary data for " + targetTenantId + " : " + e.getMessage());
        }
    }

    public void createBoundaryEntityFromFile(RequestInfo requestInfo, String targetTenantId) throws IOException {
        try{
            String hierarchyEntityCreateUri = serviceConfig.getBoundaryEntityCreateUri();

            Resource resource = resourceLoader.getResource("classpath:boundary/entity/entity.json");
            InputStream inputStream = resource.getInputStream();

            // Read file content as raw JSON string
            String rawJson = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            rawJson = rawJson.replace("{tenantid}", targetTenantId);

            JsonNode boundaryArrayNode = objectMapper.readTree(rawJson);

            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", requestInfo);
            payload.put("Boundary", objectMapper.convertValue(boundaryArrayNode, List.class));
            System.out.println(payload);

            restTemplate.postForObject(hierarchyEntityCreateUri, payload, Object.class);
            log.info("Created boundary hierarchy entity for tenant: {}", targetTenantId);
        }
        catch (Exception e) {
            log.error("Failed to create boundary hierarchy entity for tenant: {}", targetTenantId);
//            throw new CustomException("BOUNDARY_DATA_CREATE_FAILED", "Failed to create boundary data for " + targetTenantId + " : " + e.getMessage());
        }
    }

    public void createBoundaryRelationshipFromFile(RequestInfo requestInfo, String targetTenantId) throws IOException {
        try{
            String hierarchyRelationshipCreateUri = serviceConfig.getBoundaryRelationshipCreateUri();

            Resource resource = resourceLoader.getResource("classpath:boundary/relationship/relationship.json");
            InputStream inputStream = resource.getInputStream();

            // Read file content as raw JSON string
            String rawJson = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            rawJson = rawJson.replace("{tenantid}", targetTenantId);
            JsonNode relationshipArray = objectMapper.readTree(rawJson);
            JsonNode requestInfoNode = objectMapper.valueToTree(requestInfo);
            for (JsonNode relationship : relationshipArray) {
                try {
                    ObjectNode payload = objectMapper.createObjectNode();
                    payload.set("RequestInfo", requestInfoNode);
                    payload.set("BoundaryRelationship", relationship);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<JsonNode> entity = new HttpEntity<>(payload, headers);

                    createRelationshipWithRetry(payload, entity, hierarchyRelationshipCreateUri);
                   

                    log.info("Created boundary relationship entry for tenant: {}", targetTenantId);
                } catch (Exception ex) {
                    log.error("Failed to create individual boundary relationship entry for tenant: {}. Skipping...",
                            targetTenantId, ex);
                    // continue with next entry
                }
            }
            log.info("Created boundary hierarchy relationship for tenant: {}", targetTenantId);
        }
        catch (Exception e) {
            log.error("Failed to create boundary hierarchy relationship for tenant: {}", targetTenantId);
//            throw new CustomException("BOUNDARY_DATA_CREATE_FAILED", "Failed to create boundary data for " + targetTenantId + " : " + e.getMessage());
        }
    }
    
    private void waitForBoundaryData(String tenantId, RequestInfo requestInfo) {

        int maxAttempts = 10;
        int delay = 2000;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {

                if (boundaryEntityDataExists(tenantId, requestInfo)) {
                    log.info("Boundary entity data available for tenant {}", tenantId);
                    return;
                }

            } catch (Exception e) {
                log.warn("Error while checking boundary data for tenant {}", tenantId, e);
            }

            log.info("Boundary data not ready for tenant {}. Retry attempt {}", tenantId, attempt);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new RuntimeException("Boundary data not available after waiting for tenant " + tenantId);
    }
    
	private boolean boundaryEntityDataExists(String tenantId, RequestInfo requestInfo) {

		String searchUri = serviceConfig.getBoundaryEntitySearchUri();

	    UriComponentsBuilder builder = UriComponentsBuilder
	            .fromHttpUrl(searchUri)
	            .queryParam("tenantId", tenantId);

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    ObjectNode payload = objectMapper.createObjectNode();
	    payload.set("RequestInfo", objectMapper.valueToTree(requestInfo));

	    HttpEntity<JsonNode> entity = new HttpEntity<>(payload, headers);

	    ResponseEntity<JsonNode> response = restTemplate.exchange(
	            builder.toUriString(),
	            HttpMethod.POST,
	            entity,
	            JsonNode.class
	    );

	    JsonNode boundaries = response.getBody().path("Boundary");

	    return boundaries.isArray() && boundaries.size() > 0;
	}
	
	private void createRelationshipWithRetry(JsonNode payload, HttpEntity<JsonNode> entity, String uri) {
	    int retries = 5;
	    int delay = 1000;

	    for (int i = 1; i <= retries; i++) {
	        try {
	            restTemplate.postForObject(uri, entity, Object.class);
	            return;
	        } catch (HttpClientErrorException e) {
	            if (e.getResponseBodyAsString().contains("PARENT_NOT_FOUND")) {
	                log.warn("Parent not found, retrying... attempt {}", i);
	                try {
	                    Thread.sleep(delay);
	                } catch (InterruptedException ie) {
	                    Thread.currentThread().interrupt();
	                }
	            } else {
	                throw e;
	            }
	        }
	    }
	    throw new RuntimeException("Failed after retries");
	}

}
