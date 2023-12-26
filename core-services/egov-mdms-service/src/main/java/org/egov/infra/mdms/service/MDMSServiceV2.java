package org.egov.infra.mdms.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.common.utils.ResponseInfoUtil;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.ResponseUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

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
            masterDataList = mdmsDataRepository.searchV2(mdmsCriteriaReqV2.getMdmsCriteria());

            if(!CollectionUtils.isEmpty(masterDataList))
                break;
        }

        return masterDataList;
    }

    /**
     * This method is a wrapper on search to process the search request with multiple schema code.
     * @param mdmsCriteriaReqV2
     * @return
     */
    public List<MasterDataResponse> bulkSearch(MdmsCriteriaReqV2 mdmsCriteriaReqV2){

        Set<String> schemaCode = mdmsCriteriaReqV2.getMdmsCriteria().getSchemaCode();
        List<MasterDataResponse> masterDataResponses = new ArrayList<>();
        for(String code : schemaCode){

            // override the schema code in existing mdms-search-criteria
            mdmsCriteriaReqV2.getMdmsCriteria().setSchemaCode(Collections.singleton(code));
            MasterDataResponse masterDataResponse = MasterDataResponse.builder()
                    .masterData(search(mdmsCriteriaReqV2))
                    .schemaCode(code)
                    .build();

            masterDataResponses.add(masterDataResponse);
        }
        return masterDataResponses;
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
