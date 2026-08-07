package org.egov.id.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONArray;
import org.egov.id.config.CacheConfig;
import org.egov.id.model.IdRequest;
import org.egov.id.model.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsResponse;
import org.egov.mdms.service.MdmsClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that the MDMS ID-format lookup is served from the cache after the
 * first fetch, so a bulk generation run does not fan out into one MDMS call per
 * ID. Mocks the MDMS boundary ({@link MdmsClientService}) and counts how often
 * it is actually invoked.
 */
@ContextConfiguration(classes = {CacheConfig.class, MdmsService.class, MdmsServiceCacheTest.CachingConfig.class})
@ExtendWith(SpringExtension.class)
class MdmsServiceCacheTest {

    /** Turns on Spring's caching proxy and resolves the ${...} defaults in CacheConfig. */
    @EnableCaching
    static class CachingConfig {
        @Bean
        static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    private static final String TENANT = "pb.amritsar";
    private static final String ID_NAME = "boundary.city";
    private static final String FORMAT = "PB-[SEQ_TEST]";

    private static final String COMMON_MASTERS_MODULE = "common-masters";
    private static final String ID_FORMAT_MASTER = "IdFormat";

    @MockBean
    private MdmsClientService mdmsClientService;

    @Autowired
    private MdmsService mdmsService;

    private MdmsResponse formatResponse() {
        Map<String, Object> formatObj = new HashMap<>();
        formatObj.put("format", FORMAT);
        JSONArray formatArray = new JSONArray();
        formatArray.add(formatObj);
        Map<String, JSONArray> commonMasters = new HashMap<>();
        commonMasters.put(ID_FORMAT_MASTER, formatArray);
        Map<String, Map<String, JSONArray>> mdmsRes = new HashMap<>();
        mdmsRes.put(COMMON_MASTERS_MODULE, commonMasters);
        MdmsResponse response = new MdmsResponse();
        response.setMdmsRes(mdmsRes);
        return response;
    }

    @SuppressWarnings("unchecked")
    private void stubMdms() {
        when(mdmsClientService.getMaster((org.egov.common.contract.request.RequestInfo) any(), any(String.class),
                (Map<String, List<MasterDetail>>) any())).thenReturn(formatResponse());
    }

    @SuppressWarnings("unchecked")
    private void verifyMdmsCalls(int count) {
        verify(mdmsClientService, times(count)).getMaster((org.egov.common.contract.request.RequestInfo) any(),
                any(String.class), (Map<String, List<MasterDetail>>) any());
    }

    @Test
    void getIdFormat_sameKey_hitsMdmsOnceThenServesFromCache() throws Exception {
        stubMdms();
        RequestInfo requestInfo = new RequestInfo();
        IdRequest idRequest = new IdRequest(ID_NAME, TENANT, null, null);

        // Simulate a bulk run: 10 IDs for the same (tenant, idName).
        for (int i = 0; i < 10; i++) {
            assertEquals(FORMAT, mdmsService.getIdFormat(requestInfo, idRequest));
        }

        // Only the first lookup reaches MDMS; the remaining 9 are cache hits.
        verifyMdmsCalls(1);
    }

    @Test
    void getIdFormat_distinctKeys_hitMdmsOncePerKey() throws Exception {
        stubMdms();
        RequestInfo requestInfo = new RequestInfo();

        for (int i = 0; i < 5; i++) {
            mdmsService.getIdFormat(requestInfo, new IdRequest("idName-" + i, TENANT, null, null));
        }

        // Cache is keyed by tenantId:idName, so 5 distinct keys => 5 MDMS calls.
        verifyMdmsCalls(5);
    }
}
