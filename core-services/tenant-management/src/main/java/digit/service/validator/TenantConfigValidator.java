package digit.service.validator;

import digit.service.TenantService;
import digit.web.models.TenantConfigRequest;
import digit.web.models.TenantDataSearchCriteria;
import digit.web.models.TenantResponse;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class TenantConfigValidator {

    private TenantService tenantService;

    public TenantConfigValidator(TenantService tenantService) {
        this.tenantService = tenantService;
    }


    public void validateCreateReq(TenantConfigRequest tenantConfigRequest) {

        TenantResponse tenantResponse = tenantService.search(
                new TenantDataSearchCriteria().builder()
                        .code(tenantConfigRequest.getTenantConfig().getCode())
                        .build(),
                tenantConfigRequest.getRequestInfo()
        );

        if(CollectionUtils.isEmpty(tenantResponse.getTenants())){
            throw new CustomException("TENANT_NOT_FOUND","Tenant doesn't exist for given code");
        }

    }
}
