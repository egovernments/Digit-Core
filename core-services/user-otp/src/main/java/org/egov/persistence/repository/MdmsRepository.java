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

/**
 * Fetches MobileNumberValidation configs from MDMS v1.
 * The master data is a flat list: [{countryCode, mobileNumberRegex, default}, ...].
 */
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

    @Value("${egov.mdms.master.name:MobileNumberValidation}")
    private String masterName;

    @Autowired
    public MdmsRepository(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches all active MobileNumberValidation configs from MDMS for the given tenant.
     * Returns a list of flat MobileValidationConfig objects.
     * Falls back to a single default config from application.properties if MDMS returns nothing.
     */
    public List<MobileValidationConfig> fetchMobileValidationConfigs(String tenantId, RequestInfo requestInfo) {
        try {
            String uri = mdmsHost + mdmsSearchEndpoint;

            Map<String, Object> masterDetail = new HashMap<>();
            masterDetail.put("name", masterName);

            Map<String, Object> moduleDetail = new HashMap<>();
            moduleDetail.put("moduleName", moduleName);
            moduleDetail.put("masterDetails", Collections.singletonList(masterDetail));

            Map<String, Object> mdmsCriteria = new HashMap<>();
            mdmsCriteria.put("tenantId", tenantId);
            mdmsCriteria.put("moduleDetails", Collections.singletonList(moduleDetail));

            Map<String, Object> request = new HashMap<>();
            request.put("RequestInfo", requestInfo);
            request.put("MdmsCriteria", mdmsCriteria);

            log.info("Fetching {} from MDMS for tenantId: {}", masterName, tenantId);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(uri, request, Map.class);

            List<MobileValidationConfig> configs = parseResponse(response);
            if (configs.isEmpty()) {
                log.warn("No {} configs found in MDMS for tenantId: {}.", masterName, tenantId);
            }
            return configs;

        } catch (Exception e) {
            log.error("Error fetching {} from MDMS for tenantId: {}.", masterName, tenantId, e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<MobileValidationConfig> parseResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("MdmsRes")) {
            return Collections.emptyList();
        }
        Object mdmsResObj = response.get("MdmsRes");
        if (!(mdmsResObj instanceof Map)) return Collections.emptyList();

        Map<String, Object> mdmsRes = (Map<String, Object>) mdmsResObj;
        Object moduleObj = mdmsRes.get(moduleName);
        if (!(moduleObj instanceof Map)) return Collections.emptyList();

        Map<String, Object> moduleData = (Map<String, Object>) moduleObj;
        Object masterObj = moduleData.get(masterName);
        if (!(masterObj instanceof List)) return Collections.emptyList();

        List<Object> rawList = (List<Object>) masterObj;
        List<MobileValidationConfig> configs = new ArrayList<>();
        for (Object item : rawList) {
            try {
                MobileValidationConfig cfg = objectMapper.convertValue(item, MobileValidationConfig.class);
                if (cfg != null && cfg.getMobileNumberRegex() != null) {
                    configs.add(cfg);
                }
            } catch (Exception e) {
                log.warn("Skipping unparseable MobileNumberValidation entry: {}", e.getMessage());
            }
        }
        log.info("Parsed {} MobileNumberValidation configs from MDMS.", configs.size());
        return configs;
    }

}
