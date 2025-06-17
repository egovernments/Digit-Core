package digit.util;

import digit.web.models.SubTenantRequest;
import org.springframework.stereotype.Component;

@Component
public class TenantUtil {

    public String convertNameToCode(String name){

        // Convert the name to uppercase and remove all the spaces
         String code = name.toUpperCase().replace(" ", "");
         return code;
    }

    public String generateSubTenantCode(SubTenantRequest tenantRequest) {
        String parentCode = convertNameToCode(tenantRequest.getTenant().getTenantId());
        String tenantNameCode = convertNameToCode(tenantRequest.getTenant().getName());
        return parentCode + "." + tenantNameCode;
    }
}
