package digit.service.validator;

import digit.repository.TenantDataRepository;
import digit.web.models.Tenant;
import digit.web.models.TenantDataSearchCriteria;
import digit.web.models.TenantRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
public class TenantDataValidator {

    private TenantDataRepository tenantDataRepository;

    public TenantDataValidator(TenantDataRepository tenantDataRepository) {
        this.tenantDataRepository = tenantDataRepository;
    }


    public void validateCreateRequest(TenantRequest tenantRequest)
    {
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

    }

    public void validateUpdateRequest(TenantRequest tenantRequest){

        List<Tenant> tenantList = tenantDataRepository.search(TenantDataSearchCriteria
                .builder()
                .name(tenantRequest
                        .getTenant()
                        .getName())
                .build());

        // check for duplicate tenant
        if(CollectionUtils.isEmpty(tenantList)){
            throw new CustomException("RECORD NOT FOUND","Record doesn't exist");
        }

    }
}
