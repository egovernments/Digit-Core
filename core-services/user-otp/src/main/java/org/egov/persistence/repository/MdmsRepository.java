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

    public List<MobileValidationConfig> fetchMobileValidationConfigs(String tenantId, RequestInfo requestInfo) {
        try {
            String uri = mdmsHost + mdmsSearchEndpoint;

            // MDMS v2 request format
            Map<String, Object> mdmsCriteria = new HashMap<>();
            mdmsCriteria.put("tenantId", tenantId);
            mdmsCriteria.put("schemaCode", moduleName + "." + masterName);

            Map<String, Object> request = new HashMap<>();
            request.put("RequestInfo", requestInfo);
            request.put("MdmsCriteria", mdmsCriteria);

            log.info("Fetching mobile validation configs from MDMS for tenantId: {}", tenantId);

            Map<String, Object> response = restTemplate.postForObject(uri, request, Map.class);

            if (response != null && response.containsKey("mdms")) {
                Object mdmsObj = response.get("mdms");
                if (!(mdmsObj instanceof List)) {
                    log.warn("Unexpected mdms type: {}", mdmsObj != null ? mdmsObj.getClass() : "null");
                    return Collections.emptyList();
                }

                List<Object> mdmsList = (List<Object>) mdmsObj;
                List<MobileValidationConfig> configs = new ArrayList<>();

                for (Object item : mdmsList) {
                    if (!(item instanceof Map)) continue;
                    Map<String, Object> mdmsItem = (Map<String, Object>) item;
                    Object dataObj = mdmsItem.get("data");
                    if (dataObj == null) continue;
                    try {
                        MobileValidationConfig config = objectMapper.convertValue(dataObj, MobileValidationConfig.class);
                        configs.add(config);
                    } catch (Exception e) {
                        log.warn("Error converting MDMS data item to MobileValidationConfig: {}", e.getMessage());
                    }
                }

                log.info("Successfully fetched {} mobile validation configs", configs.size());
                return configs;
            }

            log.warn("Mobile validation configs not found in MDMS response for tenantId: {}", tenantId);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error fetching mobile validation configs from MDMS: ", e);
            return Collections.emptyList();
        }
    }
}
