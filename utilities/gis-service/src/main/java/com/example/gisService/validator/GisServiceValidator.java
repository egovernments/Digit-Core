package com.example.gisService.validator;

import com.example.gisService.web.models.BoundaryDataRequest;
import com.example.gisService.web.models.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GisServiceValidator {

    public void validate(BoundaryDataRequest request){

        RequestBody requestBody = request.getRequestBody();
        if(requestBody.getTenantId()==null){
            throw new CustomException("tenantId", "tennant id is mandatory");
        }

        if(requestBody.getCodes().size()==0){
            throw  new CustomException("codes","codes are mandatory");
        }
    }

}
