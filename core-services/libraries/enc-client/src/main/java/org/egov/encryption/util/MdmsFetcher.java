package org.egov.encryption.util;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.encryption.config.EncClientConstants;
import org.egov.encryption.config.EncProperties;
import org.egov.encryption.config.ErrorConstants;
import org.egov.mdms.model.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.slf4j.MDC;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MdmsFetcher {

    @Autowired
    private EncProperties encProperties;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MultiStateInstanceUtil multiStateInstanceUtil;

    public static final String TENANTID_MDC_STRING = "TENANTID";

    // Caches the MDMS master per derived state tenant, keyed by "<stateTenant>|<masterName>".
    // Only successful, non-empty fetches are cached; lives for the lifetime of the pod.
    private final ConcurrentHashMap<String, JSONArray> mdmsCache = new ConcurrentHashMap<>();

    public JSONArray getSecurityMdmsForFilter(String filter, String tenantId) {
        return getMdmsForFilter(filter, EncClientConstants.MDMS_SECURITY_POLICY_MASTER_NAME, tenantId);
    }

    public JSONArray getMaskingMdmsForFilter(String filter, String tenantId) {
        return getMdmsForFilter(filter, EncClientConstants.MDMS_MASKING_PATTERN_MASTER_NAME, tenantId);
    }

    public JSONArray getMdmsForFilter(String filter, String masterName, String tenantId) {
        // The request tenantId is mandatory - MDMS data is always resolved from it (no property fallback).
        if (!StringUtils.hasText(tenantId)) {
            throw new CustomException(ErrorConstants.TENANT_ID_REQUIRED_ERROR,
                    ErrorConstants.TENANT_ID_REQUIRED_ERROR_MESSAGE);
        }
        String stateTenant = multiStateInstanceUtil.getStateLevelTenant(tenantId);

        String cacheKey = stateTenant + "|" + masterName;

        JSONArray cached = mdmsCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        JSONArray result = fetchFromMdms(filter, masterName, stateTenant);

        // Do not cache empty results. SecurityPolicy is required, so an empty master is an error;
        // MaskingPatterns may legitimately be empty and is returned as-is without caching.
        if (CollectionUtils.isEmpty(result)) {
            if (EncClientConstants.MDMS_SECURITY_POLICY_MASTER_NAME.equals(masterName)) {
                throw new CustomException(ErrorConstants.POLICY_NOT_FOUND_ERROR,
                        masterName + ErrorConstants.POLICY_NOT_FOUND_ERROR_MESSAGE + stateTenant);
            }
            log.warn("{}{}{}", masterName, ErrorConstants.POLICY_NOT_FOUND_ERROR_MESSAGE, stateTenant);
            return result;
        }

        mdmsCache.put(cacheKey, result);
        return result;
    }

    private JSONArray fetchFromMdms(String filter, String masterName, String stateTenant) {
        MasterDetail masterDetail = MasterDetail.builder().name(masterName)
                .filter(filter).build();
        ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(EncClientConstants.MDMS_MODULE_NAME)
                .masterDetails(Arrays.asList(masterDetail)).build();
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(stateTenant)
                .moduleDetails(Arrays.asList(moduleDetail)).build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().requestInfo(RequestInfo.builder().build())
                .mdmsCriteria(mdmsCriteria).build();
        if(multiStateInstanceUtil.getIsEnvironmentCentralInstance()){
            MDC.put(TENANTID_MDC_STRING, stateTenant);
        }

        try {
            ResponseEntity<MdmsResponse> response =
                    restTemplate.postForEntity(encProperties.getEgovMdmsHost() + encProperties.getEgovMdmsSearchEndpoint(),
                            mdmsCriteriaReq, MdmsResponse.class);
            return response.getBody().getMdmsRes().get(EncClientConstants.MDMS_MODULE_NAME)
                    .get(masterName);
        } catch (Exception e) {
            log.error(ErrorConstants.MDMS_FETCH_ERROR_MESSAGE, e);
            throw new CustomException(ErrorConstants.MDMS_FETCH_ERROR, ErrorConstants.MDMS_FETCH_ERROR_MESSAGE);
        }
    }

}
