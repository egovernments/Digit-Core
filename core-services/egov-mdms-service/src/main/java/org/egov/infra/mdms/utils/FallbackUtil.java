package org.egov.infra.mdms.utils;

import net.minidev.json.JSONArray;
import org.egov.infra.mdms.model.Mdms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

        for (String subTenant : subTenantListForFallback) {
            if(tenantMasterMap.containsKey(subTenant))
                return tenantMasterMap.get(subTenant);
        }

        return new HashMap<>();
    }

    public static List<Mdms> backTrackTenantMasterDataList(List<Mdms> masterDataList, String tenantId) {
        List<Mdms> masterDataListAfterFallback = new ArrayList<>();
        List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(tenantId);

        Map<String, Map<String, List<Mdms>>> schemaGroupedTenantMasterMap = masterDataList.stream()
                .collect(Collectors.groupingBy(
                        Mdms::getSchemaCode, // First level grouping by schemaCode
                        Collectors.groupingBy(
                                Mdms::getTenantId, // Second level grouping by tenantId
                                Collectors.toList() // Collect Mdms objects into a List
                        )
                ));

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
