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
import org.egov.infra.mdms.utils.MdmsUtil;
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

    private MdmsUtil mdmsUtil;

    @Autowired
    public MDMSServiceV2(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
                         MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil, MultiStateInstanceUtil multiStateInstanceUtil, MdmsUtil mdmsUtil) {
        this.mdmsDataValidator = mdmsDataValidator;
        this.mdmsDataEnricher = mdmsDataEnricher;
        this.mdmsDataRepository = mdmsDataRepository;
        this.schemaUtil = schemaUtil;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
        this.mdmsUtil = mdmsUtil;
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
     * @param multiSchemaSearchReq
     * @return
     */
    public List<MasterDataResponse> multiSchemaSearch(MultiSchemaSearchReq multiSchemaSearchReq){

        List<MasterDataResponse> masterDataResponses = new ArrayList<>();

        // convert multi-schema-search-request to mdms-criteria-request
        MdmsCriteriaReqV2 mdmsCriteriaReqV2 = mdmsUtil.convertToMdmsCriteriaReqV2(multiSchemaSearchReq);

        for(String schemaCode: multiSchemaSearchReq.getMdmsCriteria().getSchemaCode()){

            // set schema code in mdms criteria request
            mdmsCriteriaReqV2.getMdmsCriteria().setSchemaCode(schemaCode);

            // search master-data for each schema code
            List<Mdms> masterDataList = search(mdmsCriteriaReqV2);

            // build master-data-response for each schema code
            MasterDataResponse masterDataResponse = MasterDataResponse.builder()
                    .masterData(masterDataList)
                    .schemaCode(schemaCode)
                    .build();

            // add master-data-response to list of master-data-responses
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
