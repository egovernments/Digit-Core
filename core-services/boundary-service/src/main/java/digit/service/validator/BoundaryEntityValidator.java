package digit.service.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.errors.ErrorCodes;
import digit.util.ErrorUtil;
import digit.util.GeoUtil;
import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.PointGeometry;
import digit.web.models.PolygonGeometry;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BoundaryEntityValidator {

    private final ObjectMapper objectMapper;

    @Autowired
    public BoundaryEntityValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * This method is used to validate the create boundary entity request
     * Validate for valid geometry
     * @param boundaryRequest
     */
    public void validateCreateBoundaryRequest(BoundaryRequest boundaryRequest) {
        // validate the request
        validateBoundaryGeometry(boundaryRequest.getBoundary());

    }

    /**
     * This method takes a list of boundary entities and validates geometry of
     * the boundary depending on its type.
     * @param boundaryList
     */
    private void validateBoundaryGeometry(List<Boundary> boundaryList) {
        Map<String, String> exceptions = new HashMap<>();

        boundaryList.forEach(boundary -> {
            // Only execute if geometry is present
            if(boundary.getGeometry()!=null) {
                try {
                    if(boundary.getGeometry().get("type").asText().equals("Point")) {
                        GeoUtil.validatePointGeometry(objectMapper.treeToValue(boundary.getGeometry(), PointGeometry.class), exceptions);
                    } else if(boundary.getGeometry().get("type").asText().equals("Polygon")) {
                        GeoUtil.validatePolygonGeometry(objectMapper.treeToValue(boundary.getGeometry(), PolygonGeometry.class), exceptions);
                    } else {
                        throw new CustomException(ErrorCodes.INVALID_GEOMETRY_TYPE_CODE, ErrorCodes.INVALID_GEOMETRY_TYPE_MSG);
                    }
                } catch (JsonProcessingException e) {
                    throw new CustomException(ErrorCodes.INVALID_GEOJSON_CODE, ErrorCodes.INVALID_GEOJSON_MSG);
                }
            }
        });

        ErrorUtil.throwCustomExceptions(exceptions);
    }

}
