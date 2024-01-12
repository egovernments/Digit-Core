package digit.service;

import digit.web.models.CoordinateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReverseGeocodingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String QUERY = "SELECT state_name FROM india_state_boundary WHERE st_distance(geom, st_transform('SRID=4326;POINT(%f %f)'::geometry, 102100)) = 0";

    private final String POLY_QUERY = "SELECT * FROM boundary WHERE ST_Within(ST_Transform('SRID=4326;POINT(%f %f)'::geometry, 4326),geom);";


    public String getState(CoordinateRequest body) {
        return jdbcTemplate.queryForObject(String.format(QUERY, body.getLongitude(), body.getLatitude()), String.class);
    }

    public List<String> getPolyNamesForPoint(CoordinateRequest body) {
        return jdbcTemplate.query(String.format(POLY_QUERY, body.getLongitude(), body.getLatitude()), (rs, rowNum) -> rs.getString("code"));
    }
}