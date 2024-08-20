package digit.service.validator;

import digit.config.ApplicationConfig;
import digit.repository.TenantDataRepository;
import digit.util.UserUtil;
import digit.web.models.Tenant;
import digit.web.models.TenantDataSearchCriteria;
import digit.web.models.TenantRequest;
import digit.web.models.User.UserDetailResponse;
import digit.web.models.UserRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.user.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
public class TenantDataValidator {

    private TenantDataRepository tenantDataRepository;
    private UserUtil util;
    private ApplicationConfig applicationConfig;

    public TenantDataValidator(TenantDataRepository tenantDataRepository, UserUtil util, ApplicationConfig applicationConfig) {
        this.tenantDataRepository = tenantDataRepository;
        this.util = util;
        this.applicationConfig = applicationConfig;
    }


    public void validateCreateRequest(TenantRequest tenantRequest) {
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
        if (!CollectionUtils.isEmpty(tenantList)) {
            throw new CustomException("DUPLICATE_RECORD", "Duplicate record");
        }

//        UserSearchRequest userSearchRequest = new UserSearchRequest();
//        userSearchRequest.setUserName(tenantRequest.getTenant().getEmail());
//        userSearchRequest.setTenantId("pg");
//        UserDetailResponse userDetailResponse =  util.userCall(userSearchRequest, new StringBuilder(applicationConfig.getUserSearchURI()));
//        if(!CollectionUtils.isEmpty(userDetailResponse.getUser())){
//
//            throw new CustomException("DUPLICATE_USER","User already exists with the given username");
//        }

    }

    public void validateUpdateRequest(TenantRequest tenantRequest) {

        List<Tenant> tenantList = tenantDataRepository.search(TenantDataSearchCriteria
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
