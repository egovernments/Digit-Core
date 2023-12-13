package digit.service;

import digit.web.models.CoordinateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReverseGeocodingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String QUERY = "SELECT state_name FROM india_state_boundary WHERE st_distance(geom, st_transform('SRID=4326;POINT(%f %f)'::geometry, 102100)) = 0";

    public String getState(CoordinateRequest body) {
        return jdbcTemplate.queryForObject(String.format(QUERY, body.getLongitude(), body.getLatitude()), String.class);
    }
}
