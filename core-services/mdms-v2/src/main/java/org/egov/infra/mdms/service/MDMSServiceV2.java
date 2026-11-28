package org.egov.infra.mdms.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.AuditLogRepository;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.HistoryUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class MDMSServiceV2 {

    private MdmsDataValidator mdmsDataValidator;

    private MdmsDataEnricher mdmsDataEnricher;

    private MdmsDataRepository mdmsDataRepository;

    private SchemaUtil schemaUtil;

    private MultiStateInstanceUtil multiStateInstanceUtil;

    private AuditLogRepository auditLogRepository;

    private HistoryUtil historyUtil;

    private ApplicationConfig applicationConfig;

    @Autowired
    public MDMSServiceV2(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
                         MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil, MultiStateInstanceUtil multiStateInstanceUtil,
                         AuditLogRepository auditLogRepository, HistoryUtil historyUtil, ApplicationConfig applicationConfig) {
        this.mdmsDataValidator = mdmsDataValidator;
        this.mdmsDataEnricher = mdmsDataEnricher;
        this.mdmsDataRepository = mdmsDataRepository;
        this.schemaUtil = schemaUtil;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
        this.auditLogRepository = auditLogRepository;
        this.historyUtil = historyUtil;
        this.applicationConfig = applicationConfig;
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

        return Arrays.asList(mdmsRequest.getMdms());
    }

    /**
     * This method processes the requests that come for master data search.
     * @param mdmsCriteriaReqV2
     * @return
     */
    public List<Mdms> search(MdmsCriteriaReqV2 mdmsCriteriaReqV2) {

        // Resolve the tenant level (concrete tenant or a fallback ancestor) that actually
        // has matching master data, so search results and count() agree on the same tenant.
        resolveFallbackTenant(mdmsCriteriaReqV2.getMdmsCriteria());

        return mdmsDataRepository.searchV2(mdmsCriteriaReqV2.getMdmsCriteria());
    }

    /**
     * This method processes the requests that come for master data count, using the same
     * criteria model (MdmsCriteriaReqV2) as search.
     * @param mdmsCriteriaReqV2
     * @return
     */
    public Long count(MdmsCriteriaReqV2 mdmsCriteriaReqV2) {
        return resolveFallbackTenant(mdmsCriteriaReqV2.getMdmsCriteria());
    }

    /**
     * Walks the tenant fallback chain (concrete tenantId up to state level) and mutates
     * mdmsCriteriaV2's tenantId to the first level that has matching master data, so that
     * search() and count() resolve to the exact same tenant for identical criteria.
     * @param mdmsCriteriaV2
     * @return the count of master data at the resolved tenant level
     */
    private Long resolveFallbackTenant(MdmsCriteriaV2 mdmsCriteriaV2) {
        List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(mdmsCriteriaV2.getTenantId());

        Long count = 0L;
        for (String subTenantId : subTenantListForFallback) {
            mdmsCriteriaV2.setTenantId(subTenantId);
            count = mdmsDataRepository.countV2(mdmsCriteriaV2);

            if (count > 0)
                break;
        }

        return count;
    }

    /**
     * This method processes the requests that come for master data update.
     * @param mdmsRequest
     * @param pathSchemaCode schemaCode from the request path
     * @return
     */
    public List<Mdms> update(MdmsRequest mdmsRequest, String pathSchemaCode) {

        // Fetch schema against which data is getting created
        JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest);

        // Validate master data update request
        Mdms existingRecord = mdmsDataValidator.validateUpdateRequest(mdmsRequest, schemaObject, pathSchemaCode);

        // Enrich master data update request
        mdmsDataEnricher.enrichUpdateRequest(mdmsRequest, existingRecord);

        // Emit MDMS update event to be listened by persister
        mdmsDataRepository.update(mdmsRequest);

        return Arrays.asList(mdmsRequest.getMdms());
    }

    /**
     * This method processes the requests that come for master data history search.
     * Versions of each matching record are read from audit-service.
     * @param mdmsHistoryRequest
     * @return
     */
    public MdmsHistoryResponse searchHistory(MdmsHistoryRequest mdmsHistoryRequest) {

        // Validate master data history search request
        mdmsDataValidator.validateHistoryRequest(mdmsHistoryRequest);

        MdmsHistoryCriteria historyCriteria = mdmsHistoryRequest.getHistoryCriteria();
        int offset = Objects.isNull(historyCriteria.getOffset()) ? applicationConfig.getDefaultOffset() : historyCriteria.getOffset();
        int limit = Objects.isNull(historyCriteria.getLimit()) ? Math.min(applicationConfig.getDefaultLimit(), applicationConfig.getHistoryMaxLimit()) : historyCriteria.getLimit();
        int maxVersions = Objects.isNull(historyCriteria.getMaxVersionsPerRecord()) ? applicationConfig.getHistoryMaxVersionsPerRecord() : historyCriteria.getMaxVersionsPerRecord();

        // isActive is not set, so that deactivated records are included
        MdmsCriteriaV2 mdmsCriteriaV2 = MdmsCriteriaV2.builder()
                .tenantId(historyCriteria.getTenantId())
                .schemaCode(historyCriteria.getSchemaCode())
                .uniqueIdentifiers(historyCriteria.getUniqueIdentifiers())
                .offset(offset)
                .limit(limit)
                .build();

        // Resolve the tenant (exact tenant unless fallback is requested) and count the matching records
        Long totalCount = Boolean.TRUE.equals(historyCriteria.getUseTenantFallback())
                ? resolveFallbackTenant(mdmsCriteriaV2)
                : mdmsDataRepository.countV2(mdmsCriteriaV2);

        // Fetch the matching records and build the history of each record from its audit logs
        List<MdmsHistory> mdmsHistoryList = new ArrayList<>();
        if (totalCount > 0) {
            for (Mdms mdms : mdmsDataRepository.searchV2(mdmsCriteriaV2)) {
                List<AuditLog> auditLogs = fetchAuditLogs(mdmsHistoryRequest.getRequestInfo(), mdms, maxVersions);
                mdmsHistoryList.add(historyUtil.buildHistory(mdms, auditLogs, maxVersions));
            }
        }

        return MdmsHistoryResponse.builder()
                .mdmsHistory(mdmsHistoryList)
                .pagination(Pagination.builder().offset(offset).limit(limit).totalRecords(totalCount).build())
                .build();
    }

    /**
     * This method fetches the audit logs of a master data record page by page, reading one log
     * more than maxVersions so that truncation can be detected.
     * @param requestInfo
     * @param mdms
     * @param maxVersions
     * @return
     */
    private List<AuditLog> fetchAuditLogs(RequestInfo requestInfo, Mdms mdms, int maxVersions) {
        int pageSize = applicationConfig.getAuditSearchPageSize();
        List<AuditLog> auditLogs = new ArrayList<>();
        int offset = 0;
        while (auditLogs.size() <= maxVersions) {
            List<AuditLog> auditLogPage = auditLogRepository.search(requestInfo, mdms.getTenantId(), mdms.getId(), offset, pageSize);
            auditLogs.addAll(auditLogPage);
            if (auditLogPage.size() < pageSize)
                break;
            offset += pageSize;
        }
        return auditLogs;
    }

}
