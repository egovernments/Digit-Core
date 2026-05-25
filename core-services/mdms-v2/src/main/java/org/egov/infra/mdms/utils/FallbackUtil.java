package org.egov.infra.mdms.utils;

import net.minidev.json.JSONArray;
import org.egov.infra.mdms.model.Mdms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.infra.mdms.utils.MDMSConstants.DOT_SEPARATOR;

public class FallbackUtil {

    private FallbackUtil() {}

    public static List<String> getSubTenantListForFallBack(String tenantId) {
        List<String> subTenantList = new ArrayList<>();

        subTenantList.add(tenantId);

        while(tenantId.contains(DOT_SEPARATOR)){
            tenantId = tenantId.substring(0, tenantId.lastIndexOf(DOT_SEPARATOR));
            subTenantList.add(tenantId);
        }

        return subTenantList;
    }

    public static Map<String, JSONArray> backTrackTenantMasterDataMap(Map<String, Map<String, JSONArray>> tenantMasterMap, String tenantId) {
        List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(tenantId);
        Map<String, JSONArray> masterDataPostFallBack = new HashMap<>();
        for (String subTenant : subTenantListForFallback) {
            if(tenantMasterMap.containsKey(subTenant)) {
                for (Map.Entry<String, JSONArray> entry : tenantMasterMap.get(subTenant).entrySet()) {
                    String schemaCode = entry.getKey();
                    if(!masterDataPostFallBack.containsKey(schemaCode)) {
                        masterDataPostFallBack.put(schemaCode, entry.getValue());
                    }
                }
            }
        }

        return masterDataPostFallBack;
    }

    public static List<Mdms> backTrackTenantMasterDataList(List<Mdms> masterDataList, String tenantId) {
        List<Mdms> masterDataListAfterFallback = new ArrayList<>();
        List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(tenantId);

        Map<String, List<Mdms>> schemaMasterMap = masterDataList.parallelStream().collect(Collectors.groupingBy(Mdms::getSchemaCode));

        Map<String, Map<String, List<Mdms>>> schemaGroupedTenantMasterMap = new HashMap<>();
        schemaMasterMap.keySet().forEach(schemaCode -> {
            Map<String, List<Mdms>> tenantMasterMap = schemaMasterMap.get(schemaCode).stream().collect(Collectors.groupingBy(Mdms::getTenantId));
            schemaGroupedTenantMasterMap.put(schemaCode, tenantMasterMap);
        });

        for (String schemaCode : schemaGroupedTenantMasterMap.keySet()) {
            Map<String, List<Mdms>> tenantMasterMap = schemaGroupedTenantMasterMap.get(schemaCode);

            for (String subTenant : subTenantListForFallback) {
                if (tenantMasterMap.containsKey(subTenant))
                    masterDataListAfterFallback.addAll(tenantMasterMap.get(subTenant));
            }
        }

        return masterDataListAfterFallback;
    }
}
