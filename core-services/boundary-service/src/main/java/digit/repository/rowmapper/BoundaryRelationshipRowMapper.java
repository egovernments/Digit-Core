package digit.repository.rowmapper;

import digit.web.models.*;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BoundaryRelationshipRowMapper implements ResultSetExtractor<List<BoundaryRelationshipDTO>> {

    @Override
    public List<BoundaryRelationshipDTO> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        List<BoundaryRelationshipDTO> boundaryRelationshipDTOList = new ArrayList<>();

        while(resultSet.next()) {
            AuditDetails auditDetails = AuditDetails.builder()
                    .createdBy(resultSet.getString("createdby"))
                    .createdTime(resultSet.getLong("createdtime"))
                    .lastModifiedBy(resultSet.getString("lastmodifiedby"))
                    .lastModifiedTime(resultSet.getLong("lastmodifiedtime"))
                    .build();

            BoundaryRelationshipDTO boundaryRelationshipDTO = BoundaryRelationshipDTO.builder()
                    .id(resultSet.getString("id"))
                    .tenantId(resultSet.getString("tenantid"))
                    .hierarchyType(resultSet.getString("hierarchytype"))
                    .boundaryType(resultSet.getString("boundarytype"))
                    .code(resultSet.getString("code"))
                    .parent(resultSet.getString("parent"))
                    .ancestralMaterializedPath(resultSet.getString("ancestralmaterializedpath"))
                    .auditDetails(auditDetails)
                    .build();

            boundaryRelationshipDTOList.add(boundaryRelationshipDTO);
        }

        return boundaryRelationshipDTOList;
    }
}
