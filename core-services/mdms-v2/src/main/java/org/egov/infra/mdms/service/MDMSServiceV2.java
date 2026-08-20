package org.egov.infra.mdms.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        return Arrays.asList(mdmsRequest.getMdms());
    }

}
