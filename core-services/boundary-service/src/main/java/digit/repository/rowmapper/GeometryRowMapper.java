package digit.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class GeometryRowMapper implements ResultSetExtractor<List<JsonNode>> {
    @Override
    public List<JsonNode> extractData(ResultSet resultSet) throws SQLException, DataAccessException {

        List<JsonNode> geometryList = null;
//        while (resultSet.next()) {
//
//            String id = resultSet.getString("id");
//            String type = resultSet.getString("type");
//            JsonNode coordinates = resultSet.getObject("coordinates",JsonNode.class);
//
//        }
        return geometryList;
    }
}
