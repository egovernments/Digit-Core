package digit.service;

import digit.repository.TenantDataRepository;
import digit.web.models.Tenant;
import digit.web.models.TenantDataSearchCriteria;
import digit.web.models.TenantRequest;
import digit.web.models.TenantResponse;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TenantService {

    private TenantDataRepository tenantDataRepository;

    public TenantService(TenantDataRepository tenantDataRepository) {
        this.tenantDataRepository = tenantDataRepository;
    }

    public TenantResponse create(TenantRequest tenantRequest){


        List<Tenant> tenantList = tenantDataRepository.search(TenantDataSearchCriteria
                .builder()
                .code(tenantRequest
                        .getTenant()
                        .getCode())
                .name(tenantRequest
                        .getTenant()
                        .getName())
                .build());

        // check for duplicate tenant
        if(!CollectionUtils.isEmpty(tenantList)){
            throw new CustomException("DUPLICATE_RECORD","Duplicate record");
        }

        // enrich code
        tenantRequest.getTenant().setCode(tenantRequest.getTenant().getName());
        // enrich id
        tenantRequest.getTenant().setId(String.valueOf(UUID.randomUUID()));

        tenantDataRepository.create(tenantRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo =  ResponseInfoUtil.createResponseInfoFromRequestInfo(tenantRequest.getRequestInfo(),Boolean.TRUE);
        TenantResponse tenantResponse = TenantResponse
                .builder()
                .responseInfo(responseInfo)
                .tenants((List<Tenant>) tenantRequest.getTenant())
                .build();

        return tenantResponse;
    }

}