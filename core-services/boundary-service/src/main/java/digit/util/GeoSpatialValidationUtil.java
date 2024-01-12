package digit.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.errors.ErrorCodes;

import org.egov.tracer.model.CustomException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GeoSpatialValidationUtil {

    private JdbcTemplate jdbcTemplate;

    private ObjectMapper mapper;
    String query = "SELECT ST_WITHIN(ST_GeomFromGeoJSON(?), ST_GeomFromGeoJSON(?))";

    public GeoSpatialValidationUtil(JdbcTemplate jdbcTemplate, ObjectMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    /**
     * Validates if the given child polygon is within the parent polygon
     * checks if the child polygon is within the parent polygon
     * @param childGeoJSON
     * @param parentGeoJSON
     */
    public void validatePolygonWithin(JsonNode childGeoJSON , JsonNode parentGeoJSON) {

        try {
            jdbcTemplate.query(query, new Object[]{mapper.writeValueAsString(childGeoJSON) , mapper.writeValueAsString(parentGeoJSON)}, (rs, rowNum) -> {
                if (!rs.getBoolean(1)) {
                    throw new CustomException(ErrorCodes.INVALID_POLYGON_RELATIONSHIP_CODE, ErrorCodes.INVALID_POLYGON_RELATIONSHIP_MSG);
                }
                return null;
            });

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
