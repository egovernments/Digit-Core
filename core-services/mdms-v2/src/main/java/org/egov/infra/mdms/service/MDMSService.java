package org.egov.infra.mdms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.infra.mdms.utils.MDMSConstants.*;

@Service
@Slf4j
public class MDMSService {

    private final MdmsDataValidator mdmsDataValidator;
    private final MdmsDataEnricher mdmsDataEnricher;
    private final MdmsDataRepository mdmsDataRepository;
    private final SchemaUtil schemaUtil;
    private final MdmsCacheService mdmsCacheService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MDMSService(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
                       MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil,
                       MdmsCacheService mdmsCacheService, ObjectMapper objectMapper) {
        this.mdmsDataValidator = mdmsDataValidator;
        this.mdmsDataEnricher = mdmsDataEnricher;
        this.mdmsDataRepository = mdmsDataRepository;
        this.schemaUtil = schemaUtil;
        this.mdmsCacheService = mdmsCacheService;
        this.objectMapper = objectMapper;
    }

    public List<Mdms> create(MdmsRequest mdmsRequest) {
        JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest);
        mdmsDataValidator.validateCreateRequest(mdmsRequest, schemaObject);
        mdmsDataEnricher.enrichCreateRequest(mdmsRequest, schemaObject);
        mdmsDataRepository.create(mdmsRequest);
        mdmsCacheService.evictDataCache(mdmsRequest.getMdms().getTenantId(), mdmsRequest.getMdms().getSchemaCode());
        return Arrays.asList(mdmsRequest.getMdms());
    }

    /**
     * Cache-first V1 search. Per schema code:
     *  1. Check Caffeine L1 → Redis L2 per tenant level (most-specific first).
     *  2. On full miss, singleflight deduplicates concurrent DB calls for the same key.
     *     The loader only caches non-empty results, preserving tenant fallback behaviour.
     *  3. isActive=true and JsonPath filters applied in memory on cached data.
     */
    public Map<String, Map<String, JSONArray>> search(MdmsCriteriaReq mdmsCriteriaReq) {
        String tenantId = mdmsCriteriaReq.getMdmsCriteria().getTenantId();
        Map<String, String> schemaCodes = getSchemaCodes(mdmsCriteriaReq.getMdmsCriteria());
        List<String> subTenantList = FallbackUtil.getSubTenantListForFallBack(tenantId);

        Map<String, JSONArray> resultMap = new HashMap<>();

        for (Map.Entry<String, String> entry : schemaCodes.entrySet()) {
            String schemaCode = entry.getKey();
            String filter = entry.getValue();

            // Cache-first: most-specific tenant first, fall back up the hierarchy on miss
            List<Mdms> data = null;
            for (String subTenantId : subTenantList) {
                data = mdmsCacheService.getDataFromCache(subTenantId, schemaCode);
                if (data != null) break;
            }

            if (data == null) {
                // Full cache miss — singleflight: key uses "||" to avoid collision
                String inFlightKey = subTenantList.get(0) + "||" + schemaCode;
                Map<String, List<Mdms>> byTenant = mdmsCacheService.loadWithSingleflight(inFlightKey, () -> {
                    MdmsCriteriaV2 v2Criteria = MdmsCriteriaV2.builder()
                            .tenantId(subTenantList.get(0))
                            .schemaCode(schemaCode)
                            .build();
                    List<Mdms> allData = mdmsDataRepository.searchV2ForTenants(v2Criteria, subTenantList);
                    Map<String, List<Mdms>> grouped = allData.stream()
                            .collect(Collectors.groupingBy(Mdms::getTenantId));
                    // Only cache non-empty results — preserves tenant fallback on next request
                    for (String tid : subTenantList) {
                        List<Mdms> tenantData = grouped.get(tid);
                        if (!CollectionUtils.isEmpty(tenantData)) {
                            mdmsCacheService.putDataToCache(tid, schemaCode, tenantData);
                        }
                    }
                    return grouped;
                });

                for (String subTenantId : subTenantList) {
                    List<Mdms> tenantData = byTenant.get(subTenantId);
                    if (!CollectionUtils.isEmpty(tenantData)) {
                        data = tenantData;
                        break;
                    }
                }
                if (data == null) data = Collections.emptyList();
            }

            // V1 always returns only active records
            List<Mdms> activeData = data.stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                    .collect(Collectors.toList());

            JSONArray array = convertToJSONArray(activeData);
            if (StringUtils.hasText(filter)) {
                array = filterMasters(array, filter);
            }
            resultMap.put(schemaCode, array);
        }

        return getModuleMasterMap(resultMap);
    }

    private JSONArray convertToJSONArray(List<Mdms> mdmsList) {
        JSONArray array = new JSONArray();
        mdmsList.forEach(m -> array.add(objectMapper.convertValue(m.getData(), LinkedHashMap.class)));
        return array;
    }

    private JSONArray filterMasters(JSONArray masters, String filterExp) {
        return JsonPath.read(masters, filterExp);
    }

    private Map<String, Map<String, JSONArray>> getModuleMasterMap(Map<String, JSONArray> masterMap) {
        Map<String, Map<String, JSONArray>> moduleMasterMap = new HashMap<>();
        for (Map.Entry<String, JSONArray> entry : masterMap.entrySet()) {
            String[] moduleMaster = entry.getKey().split(DOT_REGEX);
            String moduleName = moduleMaster[0];
            String masterName = moduleMaster[1];
            moduleMasterMap.computeIfAbsent(moduleName, k -> new HashMap<>())
                    .put(masterName, entry.getValue());
        }
        return moduleMasterMap;
    }

    private Map<String, String> getSchemaCodes(MdmsCriteria mdmsCriteria) {
        Map<String, String> schemaCodesFilterMap = new HashMap<>();
        for (ModuleDetail moduleDetail : mdmsCriteria.getModuleDetails()) {
            for (MasterDetail masterDetail : moduleDetail.getMasterDetails()) {
                String key = moduleDetail.getModuleName().concat(DOT_SEPARATOR).concat(masterDetail.getName());
                schemaCodesFilterMap.put(key, masterDetail.getFilter());
            }
        }
        return schemaCodesFilterMap;
    }
}
