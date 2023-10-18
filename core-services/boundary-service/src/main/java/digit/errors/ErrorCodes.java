package digit.errors;

import org.springframework.stereotype.Component;

public class ErrorCodes {

    public static final String INVALID_GEOMETRY_TYPE_CODE = "INVALID_GEOMETRY_TYPE";
    public static final String INVALID_GEOMETRY_TYPE_MSG = "Provided geometry type is not supported. Supported geometry types are Point and Polygon.";
    public static final String INVALID_GEOJSON_CODE = "INVALID_GEOJSON";
    public static final String INVALID_GEOJSON_MSG = "Provided geometry object contains invalid JSON.";

}
