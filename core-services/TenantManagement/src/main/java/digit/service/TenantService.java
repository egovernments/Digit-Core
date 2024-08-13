package digit.service;

import digit.repository.TenantDataRepository;
import digit.service.enrichment.TenantDataEnricher;
import digit.service.validator.TenantDataValidator;
import digit.web.models.Tenant;
import digit.web.models.TenantDataSearchCriteria;
import digit.web.models.TenantRequest;
import digit.web.models.TenantResponse;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class TenantService {

    private TenantDataRepository tenantDataRepository;
    private TenantDataEnricher tenantDataEnricher;
    private TenantDataValidator tenantDataValidator;


    public TenantService(TenantDataRepository tenantDataRepository, TenantDataEnricher tenantDataEnricher, TenantDataValidator tenantDataValidator) {
        this.tenantDataRepository = tenantDataRepository;
        this.tenantDataEnricher = tenantDataEnricher;
        this.tenantDataValidator = tenantDataValidator;
    }

    /**
     * Creates a tenant
     *
     * @param tenantRequest
     * @return
     */
    public TenantResponse create(TenantRequest tenantRequest) {

        // validate request
        tenantDataValidator.validateCreateRequest(tenantRequest);

        // enrich request
        tenantDataEnricher.enrichCreateReq(tenantRequest);

        // persist
        tenantDataRepository.create(tenantRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(tenantRequest.getRequestInfo(), Boolean.TRUE);
        TenantResponse tenantResponse = TenantResponse
                .builder()
                .responseInfo(responseInfo)
                .tenants(Collections.singletonList(tenantRequest.getTenant()))
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
    public TenantResponse search(TenantDataSearchCriteria searchCriteria, RequestInfo requestInfo) {

        List<Tenant> tenantList = tenantDataRepository.search(searchCriteria);

        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE);

        return TenantResponse
                .builder()
                .responseInfo(responseInfo)
                .tenants(tenantList)
                .build();
    }

    /**
     * Update tenant
     * isActive and additionalAttributes are updatable
     *
     * @param tenantRequest
     * @return
     */
    public TenantResponse update(TenantRequest tenantRequest){

        // validate
        tenantDataValidator.validateUpdateRequest(tenantRequest);

        // enrich
        tenantDataEnricher.enrichUpdateReq(tenantRequest);

        // persist
        tenantDataRepository.update(tenantRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(tenantRequest.getRequestInfo(), Boolean.TRUE);
        TenantResponse tenantResponse = TenantResponse
                .builder()
                .responseInfo(responseInfo)
                .tenants(Collections.singletonList(tenantRequest.getTenant()))
                .build();

        return tenantResponse;
    }
}



