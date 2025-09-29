package org.egov.infra.mdms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.CacheUtil;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class MDMSServiceV2 {

    private MdmsDataValidator mdmsDataValidator;

    private MdmsDataEnricher mdmsDataEnricher;

    private MdmsDataRepository mdmsDataRepository;

    private SchemaUtil schemaUtil;

    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Autowired
    private CacheUtil cacheUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public MDMSServiceV2(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
                         MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil, MultiStateInstanceUtil multiStateInstanceUtil) {
        this.mdmsDataValidator = mdmsDataValidator;
        this.mdmsDataEnricher = mdmsDataEnricher;
        this.mdmsDataRepository = mdmsDataRepository;
        this.schemaUtil = schemaUtil;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }

    /**
     * This method processes the requests that come for master data creation.
     * @param mdmsRequest
     * @param clientId
     * @return
     */
    @Transactional
    public List<Mdms> create(MdmsRequest mdmsRequest, String clientId) {
        List<Mdms> mdmsList = mdmsRequest.getMdms();
        if (mdmsList == null || mdmsList.isEmpty()) {
            throw new RuntimeException("Mdms list cannot be empty");
        }
        JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest, clientId);
        // Validate and enrich each Mdms
        for (Mdms mdms : mdmsList) {
            MdmsRequest singleReq = new MdmsRequest();
            singleReq.setMdms(List.of(mdms));
            mdmsDataValidator.validateCreateRequest(singleReq, schemaObject);
            mdmsDataEnricher.enrichCreateRequest(singleReq, schemaObject, clientId);
        }
        // All passed validation, insert all
        mdmsDataRepository.create(mdmsRequest);
        return mdmsList;
    }

    /**
     * This method processes the requests that come for master data search.
     * @param mdmsCriteriaReqV2
     * @return
     */
    public List<Mdms> search(MdmsCriteriaReqV2 mdmsCriteriaReqV2) {

        /*
         * Set incoming tenantId as state level tenantId for fallback in case master data for
         * concrete tenantId does not exist.
         */
        String tenantId = mdmsCriteriaReqV2.getMdmsCriteria().getTenantId();

        List<Mdms> masterDataList = new ArrayList<>();
        List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(tenantId);

        // Make a call to repository and get list of master data
        for(String subTenantId : subTenantListForFallback) {
            mdmsCriteriaReqV2.getMdmsCriteria().setTenantId(subTenantId);

            String cacheKey = cacheUtil.generateSHA256Key(mdmsCriteriaReqV2);

            List<Mdms> cached = cacheUtil.getFromCache(cacheKey, new TypeReference<List<Mdms>>() {});
            if (cached != null && !cached.isEmpty()) {
                masterDataList = cached;
                break;
            }

            masterDataList = mdmsDataRepository.searchV2(mdmsCriteriaReqV2.getMdmsCriteria());

            if (!CollectionUtils.isEmpty(masterDataList)) {
                cacheUtil.putToCache(cacheKey, masterDataList);
                cacheUtil.addToReverseIndexV2(cacheKey, mdmsCriteriaReqV2.getMdmsCriteria()); // To aid future invalidation
                break;
            }

        }

        return masterDataList;
    }

    /**
     * This method processes the requests that come for master data update.
     * @param mdmsRequest
     * @param clientId
     * @return
     */
    public List<Mdms> update(MdmsRequest mdmsRequest, String clientId) {

        // Fetch schema against which data is getting created
        JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest, clientId);

        // Validate master data update request
        mdmsDataValidator.validateUpdateRequest(mdmsRequest, schemaObject);

        // Enrich master data update request
        mdmsDataEnricher.enrichUpdateRequest(mdmsRequest, clientId);

        // Persist master data update directly
        mdmsDataRepository.update(mdmsRequest);

//        cacheUtil.invalidateMdmsCache(mdmsRequest.getMdms().getTenantId(),mdmsRequest.getMdms().getSchemaCode());
        Mdms first = mdmsRequest.getMdms().get(0);
        String tenantId = first.getTenantId();
        String[] parts = first.getSchemaCode().split("\\.", 2);
        String moduleName = parts.length > 0 ? parts[0] : null;
        String masterName = parts.length > 1 ? parts[1] : null;

        cacheUtil.invalidateAllByMaster(tenantId,moduleName,masterName);

        return List.of(first);
    }

}
