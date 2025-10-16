package digit.service;

import digit.repository.SubTenantDataRepository;
import digit.repository.TenantDataRepository;
import digit.service.enrichment.SubTenantDataEnricher;
import digit.service.enrichment.TenantDataEnricher;
import digit.service.validator.SubTenantDataValidator;
import digit.service.validator.TenantDataValidator;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SubTenantService {

    private SubTenantDataRepository subTenantDataRepository;
    private SubTenantDataEnricher subTenantDataEnricher;
    private SubTenantDataValidator subTenantDataValidator;


    public SubTenantService(SubTenantDataRepository subTenantDataRepository, SubTenantDataEnricher subTenantDataEnricher, SubTenantDataValidator subTenantDataValidator) {
        this.subTenantDataRepository = subTenantDataRepository;
        this.subTenantDataEnricher = subTenantDataEnricher;
        this.subTenantDataValidator = subTenantDataValidator;
    }


    public SubTenantResponse create(SubTenantRequest subTenantRequest) {

        // validate request
        subTenantDataValidator.validateCreateRequest(subTenantRequest);

        // enrich request
        subTenantDataEnricher.enrichCreateReq(subTenantRequest);

        // persist
        subTenantDataRepository.create(subTenantRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(subTenantRequest.getRequestInfo(), Boolean.TRUE);
        SubTenantResponse tenantResponse = SubTenantResponse
                .builder()
                .responseInfo(responseInfo)
                .tenants(Collections.singletonList(subTenantRequest.getTenant()))
                .build();

        return tenantResponse;
    }

    /**
     * Searches for a tenant
     * based on search criteria
     *
     * @param searchCriteria
     * @param requestInfo
     * @return
     */
    public SubTenantResponse search(SubTenantDataSearchCriteria searchCriteria, RequestInfo requestInfo) {

        List<SubTenant> tenantList = subTenantDataRepository.search(searchCriteria);

        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE);

        return SubTenantResponse
                .builder()
                .responseInfo(responseInfo)
                .tenants(tenantList)
                .build();
    }

    public SubTenantResponse update(SubTenantRequest subTenantRequest){

        // validate
        subTenantDataValidator.validateUpdateRequest(subTenantRequest);

        // enrich
        subTenantDataEnricher.enrichUpdateReq(subTenantRequest);

        // persist
        subTenantDataRepository.update(subTenantRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(subTenantRequest.getRequestInfo(), Boolean.TRUE);
        SubTenantResponse tenantResponse = SubTenantResponse
                .builder()
                .responseInfo(responseInfo)
                .tenants(Collections.singletonList(subTenantRequest.getTenant()))
                .build();

        return tenantResponse;
    }
}



