package digit.service.validator;

import digit.config.ApplicationConfig;
import digit.repository.SubTenantDataRepository;
import digit.repository.TenantDataRepository;
import digit.util.UserUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SubTenantDataValidator {

    private SubTenantDataRepository subTenantDataRepository;

    private TenantDataRepository tenantDataRepository;

    private UserUtil util;

    private ApplicationConfig applicationConfig;

    public SubTenantDataValidator(SubTenantDataRepository subTenantDataRepository, TenantDataRepository tenantDataRepository, UserUtil util, ApplicationConfig applicationConfig) {
        this.subTenantDataRepository = subTenantDataRepository;
        this.tenantDataRepository = tenantDataRepository;
        this.util = util;
        this.applicationConfig = applicationConfig;
    }


    public void validateCreateRequest(SubTenantRequest tenantRequest) {


        // check for the parentId to exist , if yes create
        List<Tenant> tenants = tenantDataRepository.search(TenantDataSearchCriteria
                .builder()
                .code(tenantRequest.getTenant().getParentId())
                .includeSubTenants(Boolean.FALSE)
                .build());

        if(CollectionUtils.isEmpty(tenants)){
            throw new CustomException("PARENT_TENANT_NOT_FOUND","Parent tenant doesn't exist");
        }

        List<SubTenant> subTenantList = subTenantDataRepository.search(SubTenantDataSearchCriteria
                .builder()
                .code(tenantRequest
                        .getTenant()
                        .getCode())
                .name(tenantRequest
                        .getTenant()
                        .getName())
                .build());

        // check for duplicate sub tenant
        if (!CollectionUtils.isEmpty(subTenantList)) {
            throw new CustomException("DUPLICATE_RECORD", "Duplicate record");
        }

    }

    public void validateUpdateRequest(SubTenantRequest tenantRequest) {

        List<SubTenant> tenantList = subTenantDataRepository.search(SubTenantDataSearchCriteria
                .builder()
                .name(tenantRequest
                        .getTenant()
                        .getName())
                .build());

        // check for duplicate tenant
        if (CollectionUtils.isEmpty(tenantList)) {
            throw new CustomException("RECORD NOT FOUND", "Record doesn't exist");
        }

    }
}
