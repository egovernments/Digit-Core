package com.example.geoJsonService.enrichment;

import com.example.geoJsonService.web.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EnrichmentService {

    public GeoJson enrichBoundaryResponse(BoundaryResponse response){
        GeoJson geoJson = new GeoJson();
        geoJson.setType("FeatureCollection");

        List<Feature> features = new ArrayList<>();


        if (response.getBoundary() != null) {
            for (Boundary boundary : response.getBoundary()) {
                Feature feature = convertToFeature(boundary);
                features.add(feature);
            }
        }

        geoJson.setFeatures(features);

        return geoJson;
    }



    private Feature convertToFeature(Boundary boundary) {
        Feature feature = new Feature();
        feature.setType("Feature");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode boundaryNode = objectMapper.valueToTree(boundary);

        if (boundaryNode.isObject()) {
            ObjectNode propertiesNode = objectMapper.createObjectNode();

            boundaryNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                if (!fieldName.equals("geometry")) {
                    propertiesNode.set(fieldName, fieldValue);
                }
            });

            feature.setProperties(propertiesNode);

            JsonNode geometryNode = boundaryNode.get("geometry");
            feature.setGeometry(geometryNode);
        }

        return feature;
    }

}
