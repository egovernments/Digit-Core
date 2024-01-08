package com.example.geoJsonService.web.controllers;

import com.example.geoJsonService.service.GeoJsonService;
import com.example.geoJsonService.web.models.BoundaryDataRequest;
import com.example.geoJsonService.web.models.GeoJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boundary/geom")
public class GeoJsonServiceController {

    @Autowired
    private GeoJsonService geoJsonService;

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public GeoJson getGeoJsonBoundaryData(@RequestBody BoundaryDataRequest requestBody){
        GeoJson geoJson = geoJsonService.convert(requestBody);
        return geoJson;
    }
}
