package com.example.geoJsonService.validator;

import com.example.geoJsonService.web.models.BoundaryDataRequest;
import com.example.geoJsonService.web.models.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GeoJsonServiceValidator {

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
