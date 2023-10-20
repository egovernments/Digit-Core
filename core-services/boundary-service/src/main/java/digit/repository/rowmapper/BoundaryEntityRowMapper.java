package digit.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.Boundary;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BoundaryEntityRowMapper implements ResultSetExtractor<List<Boundary>> {

    @Autowired
    private ObjectMapper mapper;
    @Override
    public List<Boundary> extractData(ResultSet resultSet) throws SQLException, DataAccessException {

        List<Boundary> boundaryList = new ArrayList<>();

        while (resultSet.next()) {

            AuditDetails auditDetails = AuditDetails.builder().createdBy(resultSet.getString("createdby")).
                    createdTime(resultSet.getLong("createdtime")).
                    lastModifiedBy(resultSet.getString("lastmodifiedby")).
                    lastModifiedTime(resultSet.getLong("lastmodifiedtime")).build();

            Boundary boundary = null;
            try {
                boundary = Boundary.builder()
                        .id(resultSet.getString("id"))
                        .code(resultSet.getString("code"))
                        .auditDetails(auditDetails)
                        .geometry(mapper.readTree(resultSet.getString("geometry")))
                        .tenantId(resultSet.getString("tenantid"))
                        .build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            boundaryList.add(boundary);
        }
        return boundaryList;
    }
}
