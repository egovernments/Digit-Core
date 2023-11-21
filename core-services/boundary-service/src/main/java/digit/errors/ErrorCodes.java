package digit.errors;

import org.springframework.stereotype.Component;

public class ErrorCodes {

    public static final String INVALID_GEOMETRY_TYPE_CODE = "INVALID_GEOMETRY_TYPE";
    public static final String INVALID_GEOMETRY_TYPE_MSG = "Provided geometry type is not supported. Supported geometry types are Point and Polygon.";
    public static final String INVALID_GEOJSON_CODE = "INVALID_GEOJSON";
    public static final String INVALID_GEOJSON_MSG = "Provided geometry object contains invalid JSON.";
    public static final String DUPLICATE_CODE_CODE = "DUPLICATE_CODE";
    public static final String DUPLICATE_CODE_MSG = "Boundary entity with same code and tenantId already exists.";
    public static final String NOT_FOUND_CODE_AND_TENANT_ID_CODE = "NOT_FOUND_CODE_AND_TENANT_ID_CODE";
    public static final String NOT_FOUND_CODE_AND_TENANT_ID_MSG = "Tenant Id and Code combination not found.";
    public static final String INVALID_POSITION_CODE = "INVALID_POSITION";
    public static final String INVALID_POSITION_MSG = "Position array must contain 2 values";
    public static final String INVALID_POLYGON_CODE = "INVALID_POLYGON";
    public static final String INVALID_POLYGON_MSG = "Polygon must not be empty neither should it contain any holes.";
    public static final String INVALID_POLYGON_COORDINATES_DEFINITION_CODE = "INVALID_POLYGON_COORDINATES_DEFINITION";
    public static final String INVALID_POLYGON_COORDINATES_DEFINITION_MSG = "Polygon must be defined with minimum 4 coordinates.";
    public static final String INVALID_POLYGON_DEFINITION_CODE = "INVALID_POLYGON_DEFINITION";
    public static final String INVALID_POLYGON_DEFINITION_MSG = "Polygon coordinates must begin and end with the same coordinate according to RFC 7946 standard.";
    public static final String INVALID_HIERARCHY_DEFINITION_CODE = "INVALID_HIERARCHY_DEFINITION";
    public static final String INVALID_HIERARCHY_DEFINITION_MSG = "Given parent type is not part of boundary hierarchy definition - ";
    public static final String INVALID_HIERARCHY_ENTITY_DEFINITION_CODE = "INVALID_HIERARCHY_DEFINITION";
    public static final String INVALID_HIERARCHY_ENTITY_DEFINITION_MSG = "Hierarchy entities must not form a cycle.";
    public static final String DUPLICATE_RECORD_CODE = "DUPLICATE_RECORD";
    public static final String DUPLICATE_RECORD_MSG = "Boundary hierarchy with the provided tenantId and hierarchy type already exists.";
    public static final String MULTIPLE_ROOT_NODES_ERR_CODE = "MULTIPLE_ROOT_NODES_ERR";
    public static final String MULTIPLE_ROOT_NODES_ERR_MSG = "Hierarchy definition must have only one root node.";
    public static final String DUPLICATE_BOUNDARY_CODE = "DUPLICATE_BOUNDARY";
    public static final String DUPLICATE_BOUNDARY_MSG = "Duplicate boundary entities found in the request.";

}
