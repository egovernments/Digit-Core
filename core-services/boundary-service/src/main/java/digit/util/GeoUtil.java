package digit.util;

import digit.errors.ErrorCodes;
import digit.web.models.PointGeometry;
import digit.web.models.PolygonGeometry;
import org.egov.tracer.model.CustomException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GeoUtil {

    private GeoUtil() {}

    public static void validatePointGeometry(PointGeometry pointGeometry) {
        validatePositions(Collections.singletonList(pointGeometry.getCoordinates()));
    }

    public static void validatePolygonGeometry(PolygonGeometry polygonGeometry) {
        validateIfPolygonIsSimple(polygonGeometry.getCoordinates());
        validatePositions(polygonGeometry.getCoordinates().get(0));
        validateIfPolygonIsClosed(polygonGeometry.getCoordinates().get(0));
    }

    private static void validateIfPolygonIsSimple(List<List<List<Double>>> coordinates) {
        if(coordinates.size() != 1) {
            throw new CustomException(ErrorCodes.INVALID_POLYGON_CODE,ErrorCodes.INVALID_POLYGON_MSG);
        }

        if(coordinates.get(0).size() < 5) {
            throw new CustomException(ErrorCodes.INVALID_POLYGON_COORDINATES_DEFINITION_CODE, ErrorCodes.INVALID_POLYGON_COORDINATES_DEFINITION_MSG);
        }
    }

    private static void validatePositions(List<List<Double>> coordinatesList) {
        coordinatesList.forEach(coordinate -> {
            if(coordinate.size() != 2) {
                throw new CustomException(ErrorCodes.INVALID_POSITION_CODE, ErrorCodes.INVALID_POSITION_MSG);
            }
        });
    }

    private static void validateIfPolygonIsClosed(List<List<Double>> coordinatesList) {
        if(coordinatesList.size() >= 5) {
            List<Double> startCoordinate = coordinatesList.get(0);
            List<Double> endCoordinate = coordinatesList.get(coordinatesList.size() - 1);
            if(!Objects.equals(startCoordinate.get(0), endCoordinate.get(0)) || !Objects.equals(startCoordinate.get(1), endCoordinate.get(1))) {
                throw new CustomException(ErrorCodes.INVALID_POLYGON_DEFINITION_CODE, ErrorCodes.INVALID_POLYGON_DEFINITION_MSG);
            }
        }
    }

}
