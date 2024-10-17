package org.egov.handler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.*;
import org.egov.handler.web.models.*;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import static org.egov.handler.config.ServiceConstants.TENANT_BOUNDARY_SCHEMA;
import static org.egov.handler.constants.UserConstants.*;

@Slf4j
@Service
public class DataHandlerService {

    private final MdmsV2Util mdmsV2Util;

    private final HrmsUtil hrmsUtil;

    private final LocalizationUtil localizationUtil;

    private final TenantManagementUtil tenantManagementUtil;

    private final ServiceConfiguration serviceConfig;

    private final ObjectMapper objectMapper;

    private final ResourceLoader resourceLoader;

    private final WorkflowUtil workflowUtil;

    private final CustomKafkaTemplate producer;

    @Autowired
    public DataHandlerService(MdmsV2Util mdmsV2Util, HrmsUtil hrmsUtil, LocalizationUtil localizationUtil, TenantManagementUtil tenantManagementUtil, ServiceConfiguration serviceConfig, ObjectMapper objectMapper, ResourceLoader resourceLoader, WorkflowUtil workflowUtil, CustomKafkaTemplate producer) {
        this.mdmsV2Util = mdmsV2Util;
        this.hrmsUtil = hrmsUtil;
        this.localizationUtil = localizationUtil;
        this.tenantManagementUtil = tenantManagementUtil;
        this.serviceConfig = serviceConfig;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.workflowUtil = workflowUtil;
        this.producer = producer;
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

        if (defaultDataRequest.getLocales() != null && defaultDataRequest.getModules() != null) {
            for (String locale : defaultDataRequest.getLocales()) {
                DefaultLocalizationDataRequest defaultLocalizationDataRequest = DefaultLocalizationDataRequest.builder().requestInfo(defaultDataRequest.getRequestInfo()).targetTenantId(defaultDataRequest.getTargetTenantId()).locale(locale).modules(defaultDataRequest.getModules()).defaultTenantId(serviceConfig.getDefaultTenantId()).build();
                localizationUtil.createLocalizationData(defaultLocalizationDataRequest);
            }
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

        Resource resource = resourceLoader.getResource(WELCOME_MAIL_CLASSPATH);
        String emailBody = "";
        try {
            emailBody = resource.getContentAsString(Charset.defaultCharset());
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

}
