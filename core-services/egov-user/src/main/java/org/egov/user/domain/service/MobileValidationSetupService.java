package org.egov.user.domain.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.user.domain.model.mdmsv2.MdmsV2Data;
import org.egov.user.domain.model.mdmsv2.MdmsV2Response;
import org.egov.user.domain.model.mdmsv2.MdmsV2SearchCriteria;
import org.egov.user.domain.model.mdmsv2.MdmsV2SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MobileValidationSetupService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${egov.mdms.v2.host}")
    private String mdmsHost;

    @Value("${egov.mdms.v2.search.endpoint}")
    private String searchEndpoint;

    @Value("${egov.mdms.v2.schema.create.endpoint:/mdms-v2/schema/v1/_create}")
    private String schemaCreateEndpoint;

    @Value("${egov.mdms.v2.data.create.endpoint:/mdms-v2/v2/_create}")
    private String dataCreateEndpoint;

    @Value("${egov.mobile.validation.schema.code:common-masters.MobileNumberValidation}")
    private String schemaCode;

    @Value("${egov.mobile.validation.default.country.code:+91}")
    private String defaultCountryCode;

    @Value("${egov.mobile.validation.default.regex:^[6-9][0-9]{9}$}")
    private String defaultRegex;

    @Value("${state.level.tenant.id:pg}")
    private String stateLevelTenantId;

    @PostConstruct
    public void ensureSchemaAndDataExist() {
        try {
            RequestInfo systemRequestInfo = buildSystemRequestInfo();

            boolean schemaPresent = isDefaultDataPresent(stateLevelTenantId, systemRequestInfo);
            if (!schemaPresent) {
                log.info("MobileNumberValidation data not found for tenantId: {}. Attempting to create schema and default data.", stateLevelTenantId);
                createSchema(systemRequestInfo);
                createDefaultData(systemRequestInfo);
            } else {
                log.info("MobileNumberValidation schema/data already present for tenantId: {}", stateLevelTenantId);
            }
        } catch (Exception e) {
            log.error("MobileValidationSetupService: startup initialization failed. Service will use application.properties fallback for validation.", e);
        }
    }

    private boolean isDefaultDataPresent(String tenantId, RequestInfo requestInfo) {
        try {
            MdmsV2SearchRequest req = MdmsV2SearchRequest.builder()
                    .mdmsCriteria(MdmsV2SearchCriteria.builder()
                            .tenantId(tenantId)
                            .schemaCode(schemaCode)
                            .limit(1)
                            .offset(0)
                            .build())
                    .requestInfo(requestInfo)
                    .build();

            MdmsV2Response response = restTemplate.postForObject(
                    mdmsHost + searchEndpoint, req, MdmsV2Response.class);

            if (response == null) return false;
            List<MdmsV2Data> mdms = response.getMdms();
            return !CollectionUtils.isEmpty(mdms);

        } catch (Exception e) {
            log.warn("Could not check MDMS for existing MobileNumberValidation data: {}", e.getMessage());
            return false;
        }
    }

    private void createSchema(RequestInfo requestInfo) {
        try {
            ObjectNode definition = objectMapper.createObjectNode();
            definition.put("type", "object");
            definition.put("title", "Mobile Number Validation");
            definition.put("$schema", "http://json-schema.org/draft-07/schema#");
            definition.set("required", objectMapper.createArrayNode().add("countryCode").add("mobileNumberRegex"));
            definition.set("x-unique", objectMapper.createArrayNode().add("countryCode"));

            ObjectNode properties = objectMapper.createObjectNode();
            properties.set("tenantId", objectMapper.createObjectNode().put("type", "string"));
            properties.set("countryCode", objectMapper.createObjectNode().put("type", "string"));
            properties.set("mobileNumberRegex", objectMapper.createObjectNode().put("type", "string"));
            ObjectNode defaultProp = objectMapper.createObjectNode();
            defaultProp.put("type", "boolean");
            defaultProp.put("default", false);
            properties.set("default", defaultProp);
            definition.set("properties", properties);
            definition.set("x-ref-schema", objectMapper.createArrayNode());
            definition.put("additionalProperties", false);

            ObjectNode schemaDef = objectMapper.createObjectNode();
            schemaDef.put("tenantId", stateLevelTenantId);
            schemaDef.put("code", schemaCode);
            schemaDef.put("description", "Mobile Number Validation Configuration");
            schemaDef.set("definition", definition);
            schemaDef.put("isActive", true);

            Map<String, Object> body = new HashMap<>();
            body.put("RequestInfo", requestInfo);
            body.put("SchemaDefinition", schemaDef);

            restTemplate.postForObject(mdmsHost + schemaCreateEndpoint, body, Object.class);
            log.info("Created MobileNumberValidation schema in MDMS.");
        } catch (Exception e) {
            log.error("Failed to create MobileNumberValidation schema: {}", e.getMessage());
        }
    }

    private void createDefaultData(RequestInfo requestInfo) {
        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("countryCode", defaultCountryCode);
            data.put("mobileNumberRegex", defaultRegex);
            data.put("default", true);

            Map<String, Object> mdmsEntry = new HashMap<>();
            mdmsEntry.put("tenantId", stateLevelTenantId);
            mdmsEntry.put("schemaCode", schemaCode);
            mdmsEntry.put("isActive", true);
            mdmsEntry.put("data", data);

            Map<String, Object> body = new HashMap<>();
            body.put("RequestInfo", requestInfo);
            body.put("Mdms", mdmsEntry);

            restTemplate.postForObject(mdmsHost + dataCreateEndpoint + "/" + schemaCode, body, Object.class);
            log.info("Created default MobileNumberValidation data in MDMS for tenantId: {} countryCode: {}",
                    stateLevelTenantId, defaultCountryCode);
        } catch (Exception e) {
            log.error("Failed to create default MobileNumberValidation data: {}", e.getMessage());
        }
    }

    private RequestInfo buildSystemRequestInfo() {
        User systemUser = User.builder()
                .uuid("egov-user-system")
                .userName("egov-user-system")
                .type("SYSTEM")
                .tenantId(stateLevelTenantId)
                .build();
        return RequestInfo.builder()
                .apiId("egov-user")
                .action("_setup")
                .ver("1.0")
                .userInfo(systemUser)
                .build();
    }
}
