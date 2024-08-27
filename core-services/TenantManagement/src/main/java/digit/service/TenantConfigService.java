package digit.service;

import digit.repository.TenantConfigRepository;
import digit.service.enrichment.TenantConfigEnricher;
import digit.service.validator.TenantConfigValidator;
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
public class TenantConfigService {

    private TenantConfigRepository tenantConfigRepository;

    private TenantConfigEnricher tenantConfigEnricher;

    private TenantConfigValidator tenantConfigValidator;


    public TenantConfigService(TenantConfigRepository tenantConfigRepository, TenantConfigEnricher tenantConfigEnricher, TenantConfigValidator tenantConfigValidator) {
        this.tenantConfigRepository = tenantConfigRepository;
        this.tenantConfigEnricher = tenantConfigEnricher;
        this.tenantConfigValidator = tenantConfigValidator;
    }

    /**
     * @param tenantConfigRequest
     * @return
     */
    public TenantConfigResponse create(TenantConfigRequest tenantConfigRequest) {

        // validate request
        tenantConfigValidator.validateCreateReq(tenantConfigRequest);

        // enrich request
        tenantConfigEnricher.enrichCreateReq(tenantConfigRequest);

        // persist
        tenantConfigRepository.create(tenantConfigRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(tenantConfigRequest.getRequestInfo(), Boolean.TRUE);
        TenantConfigResponse tenantConfigResponse = new TenantConfigResponse().builder()
                .tenantConfigs(Collections.singletonList(tenantConfigRequest.getTenantConfig()))
                .responseInfo(responseInfo)
                .build();

        return tenantConfigResponse;
    }

    /**
     * Searches for a tenant
     * based on search criteria
     *
     * @param searchCriteria
     * @param requestInfo
     * @return
     */
    public TenantConfigResponse search(TenantConfigSearchCriteria searchCriteria, RequestInfo requestInfo) {

        List<TenantConfig> tenantList = tenantConfigRepository.search(searchCriteria);

        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE);

        return TenantConfigResponse
                .builder()
                .responseInfo(responseInfo)
                .tenantConfigs(tenantList)
                .build();
    }

    /**
     * @param tenantConfigRequest
     * @return
     */
    public TenantConfigResponse update(TenantConfigRequest tenantConfigRequest) {

        // validate
        // tenantConfigValidator.validateUpdateRequest(tenantRequest);

        // enrich
        tenantConfigEnricher.enrichUpdateReq(tenantConfigRequest);

        // persist
        tenantConfigRepository.update(tenantConfigRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(tenantConfigRequest.getRequestInfo(), Boolean.TRUE);
        return TenantConfigResponse
                .builder()
                .responseInfo(responseInfo)
                .tenantConfigs(Collections.singletonList(tenantConfigRequest.getTenantConfig()))
                .build();

    }
}
