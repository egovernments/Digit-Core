package digit.service.validator;

import digit.web.models.TenantConfigRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TenantConfigValidator {

    public void validateCreateReq(TenantConfigRequest tenantConfigRequest){

        // check for root tenant

    }
}
