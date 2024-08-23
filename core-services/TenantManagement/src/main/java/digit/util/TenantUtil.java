package digit.util;

import digit.web.models.SubTenantRequest;
import org.springframework.stereotype.Component;

@Component
public class TenantUtil {

    public String convertNameToCode(String name){

        // Convert the name to uppercase and replace spaces with underscores
        // String code = name.toUpperCase().replace(" ", "_");

        return name;
    }

    public String generateSubTenantCode(SubTenantRequest tenantRequest) {
        String parentCode = convertNameToCode(tenantRequest.getTenant().getParentId());
        String tenantNameCode = convertNameToCode(tenantRequest.getTenant().getName());
        return parentCode + "." + tenantNameCode;
    }
}
