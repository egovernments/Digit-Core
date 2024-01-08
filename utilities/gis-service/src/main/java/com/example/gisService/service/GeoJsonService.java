package com.example.gisService.service;

import com.example.gisService.enrichment.EnrichmentService;
import com.example.gisService.utils.BoundaryServiceUtil;
import com.example.gisService.validator.GisServiceValidator;
import com.example.gisService.web.models.*;
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
    private GisServiceValidator geoJsonServiceValidator;

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
