package com.example.geoJsonService.service;

import com.example.geoJsonService.enrichment.EnrichmentService;
import com.example.geoJsonService.utils.BoundaryServiceUtil;
import com.example.geoJsonService.validator.GeoJsonServiceValidator;
import com.example.geoJsonService.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeoJsonService {

    @Autowired
    private BoundaryServiceUtil boundaryServiceUtil;

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private GeoJsonServiceValidator geoJsonServiceValidator;

    public GeoJson convert(BoundaryDataRequest request){

        geoJsonServiceValidator.validate(request);

        BoundaryResponse result = boundaryServiceUtil.fetchResult(request);

        if (result.getBoundary() != null && result.getBoundary().size()==0) {
            throw new CustomException("Codes","no data available for these codes");
        }

        GeoJson response =  enrichmentService.enrichBoundaryResponse(result);

        return response;
    }

}
