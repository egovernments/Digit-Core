package digit.util;

import digit.web.models.PointGeometry;
import digit.web.models.PolygonGeometry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GeoUtil {

    private GeoUtil() {}

    public static void validatePointGeometry(PointGeometry pointGeometry, Map<String, String> exceptions) {
        validatePositions(Collections.singletonList(pointGeometry.getCoordinates()), exceptions);
    }

    public static void validatePolygonGeometry(PolygonGeometry polygonGeometry, Map<String, String> exceptions) {
        validateIfPolygonIsSimple(polygonGeometry.getCoordinates(), exceptions);
        if(polygonGeometry.getCoordinates().size() == 1) {
            validatePositions(polygonGeometry.getCoordinates().get(0), exceptions);
            validateIfPolygonIsClosed(polygonGeometry.getCoordinates().get(0), exceptions);
        }
    }

    private static void validateIfPolygonIsSimple(List<List<List<Double>>> coordinates, Map<String, String> exceptions) {
        if(coordinates.size() != 1) {
            exceptions.put("INVALID_POLYGON", "Polygon must not be empty neither should it contain any holes.");
            return;
        }

        if(coordinates.get(0).size() < 5) {
            exceptions.put("INVALID_POLYGON_COORDINATES_DEFINITION", "Polygon must be defined with minimum 4 coordinates.");
            return;
        }
    }

    private static void validatePositions(List<List<Double>> coordinatesList, Map<String, String> exceptions) {
        coordinatesList.forEach(coordinate -> {
            if(coordinate.size() != 2) {
                exceptions.put("INVALID_POSITION", "Position array must contain 2 values");
            }
        });
    }

    private static void validateIfPolygonIsClosed(List<List<Double>> coordinatesList, Map<String, String> exceptions) {
        if(coordinatesList.size() >= 5) {
            List<Double> startCoordinate = coordinatesList.get(0);
            List<Double> endCoordinate = coordinatesList.get(coordinatesList.size() - 1);
            if(startCoordinate.get(0) != endCoordinate.get(0) || startCoordinate.get(1) != endCoordinate.get(1)) {
                exceptions.put("INVALID_POLYGON_DEFINITION", "Polygon coordinates must begin and end with the same coordinate according to RFC 7946 standard.");
            }
        }
    }

}
