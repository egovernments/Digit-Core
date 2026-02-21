package org.egov.persistence.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.domain.model.MobileValidationConfig;
import org.egov.web.contract.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Repository
@Slf4j
public class MdmsRepository {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsSearchEndpoint;

    @Value("${egov.mdms.module.name:common-masters}")
    private String moduleName;

    @Value("${egov.mdms.master.name:UserValidation}")
    private String masterName;

    @Autowired
    public MdmsRepository(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public MobileValidationConfig fetchMobileValidationConfig(String tenantId, RequestInfo requestInfo) {
        try {
            String uri = mdmsHost + mdmsSearchEndpoint;

            Map<String, Object> masterDetail = new HashMap<>();
            masterDetail.put("name", masterName);

            List<Map<String, Object>> masterDetails = new ArrayList<>();
            masterDetails.add(masterDetail);

            Map<String, Object> moduleDetail = new HashMap<>();
            moduleDetail.put("moduleName", moduleName);
            moduleDetail.put("masterDetails", masterDetails);

            List<Map<String, Object>> moduleDetails = new ArrayList<>();
            moduleDetails.add(moduleDetail);

            Map<String, Object> mdmsCriteria = new HashMap<>();
            mdmsCriteria.put("tenantId", tenantId);
            mdmsCriteria.put("moduleDetails", moduleDetails);

            Map<String, Object> request = new HashMap<>();
            request.put("RequestInfo", requestInfo);
            request.put("MdmsCriteria", mdmsCriteria);

            log.info("Fetching mobile validation config from MDMS for tenantId: {}", tenantId);

            Map<String, Object> response = restTemplate.postForObject(uri, request, Map.class);

            if (response != null && response.containsKey("MdmsRes")) {
                Object mdmsResObj = response.get("MdmsRes");
                if (!(mdmsResObj instanceof Map)) {
                    log.warn("Unexpected MdmsRes type: {}", mdmsResObj != null ? mdmsResObj.getClass() : "null");
                    return null;
                }
                Map<String, Object> mdmsRes = (Map<String, Object>) mdmsResObj;

                if (mdmsRes.containsKey(moduleName)) {
                    Object moduleObj = mdmsRes.get(moduleName);
                    if (!(moduleObj instanceof Map)) {
                        log.warn("Unexpected module type for {}: {}", moduleName, moduleObj != null ? moduleObj.getClass() : "null");
                        return null;
                    }
                    Map<String, Object> validationConfigs = (Map<String, Object>) moduleObj;

                    if (validationConfigs.containsKey(masterName)) {
                        Object masterObj = validationConfigs.get(masterName);
                        if (!(masterObj instanceof List)) {
                            log.warn("Unexpected master type for {}: {}", masterName, masterObj != null ? masterObj.getClass() : "null");
                            return null;
                        }
                        List<Object> configList = (List<Object>) masterObj;

                        if (!configList.isEmpty()) {
                            MobileValidationConfig config = objectMapper.convertValue(
                                    configList.get(0), MobileValidationConfig.class);
                            log.info("Successfully fetched mobile validation config: {}", config);
                            return config;
                        }
                    }
                }
            }

            log.warn("Mobile validation config not found in MDMS response for tenantId: {}", tenantId);
            return null;

        } catch (Exception e) {
            log.error("Error fetching mobile validation config from MDMS: ", e);
            return null;
        }
    }
}
