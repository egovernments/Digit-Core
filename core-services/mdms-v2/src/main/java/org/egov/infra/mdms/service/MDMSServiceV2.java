package org.egov.infra.mdms.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.config.ApplicationConfig;
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
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MDMSServiceV2 {

    private MdmsDataValidator mdmsDataValidator;
    private MdmsDataEnricher mdmsDataEnricher;
    private MdmsDataRepository mdmsDataRepository;
    private SchemaUtil schemaUtil;
    private MultiStateInstanceUtil multiStateInstanceUtil;
    private MdmsCacheService mdmsCacheService;
    private ApplicationConfig config;

    @Autowired
    public MDMSServiceV2(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
                         MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil,
                         MultiStateInstanceUtil multiStateInstanceUtil, MdmsCacheService mdmsCacheService,
                         ApplicationConfig config) {
        this.mdmsDataValidator = mdmsDataValidator;
        this.mdmsDataEnricher = mdmsDataEnricher;
        this.mdmsDataRepository = mdmsDataRepository;
        this.schemaUtil = schemaUtil;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
        this.mdmsCacheService = mdmsCacheService;
        this.config = config;
    }

    /**
     * This method processes the requests that come for master data creation.
     * @param mdmsRequest
     * @return
     */
    public List<Mdms> create(MdmsRequest mdmsRequest) {

        // Fetch schema against which data is getting created
        JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest);

        // Perform validations on incoming request
        mdmsDataValidator.validateCreateRequest(mdmsRequest, schemaObject);

        // Enrich incoming master data
        mdmsDataEnricher.enrichCreateRequest(mdmsRequest, schemaObject);

        // Emit MDMS create event to be listened by persister
        mdmsDataRepository.create(mdmsRequest);

        mdmsCacheService.evictDataCache(mdmsRequest.getMdms().getTenantId(), mdmsRequest.getMdms().getSchemaCode());

        return Arrays.asList(mdmsRequest.getMdms());
    }

    /**
     * This method processes the requests that come for master data search.
     * Cache-first: checks Redis per tenant level (most-specific first). On full miss,
     * fires a single DB query with tenantid IN (...) covering all fallback levels, then
     * caches each level's results separately for subsequent requests.
     */
    public List<Mdms> search(MdmsCriteriaReqV2 mdmsCriteriaReqV2) {
        MdmsCriteriaV2 criteria = mdmsCriteriaReqV2.getMdmsCriteria();
        if (log.isDebugEnabled()) {
            log.debug("V2 search params: tenantId={} schemaCode={} ids={} uniqueIdentifiers={} filterMap={} isActive={} offset={} limit={}",
                    criteria.getTenantId(), criteria.getSchemaCode(), criteria.getIds(),
                    criteria.getUniqueIdentifiers(), criteria.getFilterMap(), criteria.getIsActive(),
                    criteria.getOffset(), criteria.getLimit());
        }
        String tenantId = criteria.getTenantId();
        String schemaCode = criteria.getSchemaCode();

        List<String> subTenantList = FallbackUtil.getSubTenantListForFallBack(tenantId);

        // Cache is only safe for unfiltered schema lookups. Additional filters (filterMap, ids,
        // uniqueIdentifiers, isActive) produce a partial result that must not be cached against
        // the full tenantId+schemaCode key — it would corrupt subsequent unfiltered requests.
        boolean isSimpleLookup = !ObjectUtils.isEmpty(schemaCode)
                && Objects.isNull(criteria.getIds())
                && Objects.isNull(criteria.getUniqueIdentifiers())
                && CollectionUtils.isEmpty(criteria.getFilterMap())
                && Objects.isNull(criteria.getIsActive());

        if (isSimpleLookup) {
            for (String subTenantId : subTenantList) {
                List<Mdms> cached = mdmsCacheService.getDataFromCache(subTenantId, schemaCode);
                if (cached != null) {
                    return applyPagination(cached, criteria);
                }
            }

            // Full cache miss — single DB query across all tenant levels
            List<Mdms> allData = mdmsDataRepository.searchV2ForTenants(criteria, subTenantList);

            // Cache each level separately (including empty lists, to avoid repeated DB hits for missing tenants)
            Map<String, List<Mdms>> byTenant = allData.stream()
                    .collect(Collectors.groupingBy(Mdms::getTenantId));
            for (String subTenantId : subTenantList) {
                mdmsCacheService.putDataToCache(subTenantId, schemaCode,
                        byTenant.getOrDefault(subTenantId, Collections.emptyList()));
            }

            // Return data from most-specific tenant that has results
            for (String subTenantId : subTenantList) {
                List<Mdms> tenantData = byTenant.get(subTenantId);
                if (!CollectionUtils.isEmpty(tenantData)) {
                    return applyPagination(tenantData, criteria);
                }
            }
            return Collections.emptyList();
        }

        // Filtered or non-schemaCode queries: original sequential DB path
        for (String subTenantId : subTenantList) {
            criteria.setTenantId(subTenantId);
            List<Mdms> result = mdmsDataRepository.searchV2(criteria);
            if (!CollectionUtils.isEmpty(result)) {
                return result;
            }
        }
        criteria.setTenantId(tenantId); // restore original
        return Collections.emptyList();
    }

    private List<Mdms> applyPagination(List<Mdms> data, MdmsCriteriaV2 criteria) {
        int offset = criteria.getOffset() != null ? criteria.getOffset() : 0;
        int requestedLimit = criteria.getLimit() != null ? criteria.getLimit() : data.size();
        int limit = resolveEffectiveLimit(criteria.getSchemaCode(), requestedLimit, data.size());
        if (offset == 0 && limit >= data.size()) return data;
        return data.stream().skip(offset).limit(limit).collect(Collectors.toList());
    }

    private int resolveEffectiveLimit(String schemaCode, int requestedLimit, int dataSize) {
        if (schemaCode != null && config.getNoLimitSchemaCodes().contains(schemaCode)) {
            return requestedLimit;
        }
        return Math.min(requestedLimit, config.getSearchResultLimit());
    }

    /**
     * This method processes the requests that come for master data update.
     * @param mdmsRequest
     * @return
     */
    public List<Mdms> update(MdmsRequest mdmsRequest) {

        // Fetch schema against which data is getting created
        JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest);

        // Validate master data update request
        mdmsDataValidator.validateUpdateRequest(mdmsRequest, schemaObject);

        // Enrich master data update request
        mdmsDataEnricher.enrichUpdateRequest(mdmsRequest);

        // Emit MDMS update event to be listened by persister
        mdmsDataRepository.update(mdmsRequest);

        mdmsCacheService.evictDataCache(mdmsRequest.getMdms().getTenantId(), mdmsRequest.getMdms().getSchemaCode());

        return Arrays.asList(mdmsRequest.getMdms());
    }

}
