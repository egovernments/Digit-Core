package digit.repository.rowmapper;

import digit.web.models.BoundaryIdentifier;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BoundaryIdentifierRowMapper implements ResultSetExtractor<List<BoundaryIdentifier>> {

    @Override
    public List<BoundaryIdentifier> extractData(ResultSet resultSet) throws SQLException, DataAccessException {

        List<BoundaryIdentifier> boundaryIdentifierList = new ArrayList<>();

        while (resultSet.next()) {

            AuditDetails auditDetails = AuditDetails.builder().createdBy(resultSet.getString("createdby")).
                    createdTime(resultSet.getLong("createdtime")).
                    lastModifiedBy(resultSet.getString("lastmodifiedby")).
                    lastModifiedTime(resultSet.getLong("lastmodifiedtime")).build();

            boundaryIdentifierList.add(BoundaryIdentifier.builder()
                    .id(resultSet.getString("id"))
                    .tenantId(resultSet.getString("tenantid"))
                    .hierarchyType(resultSet.getString("hierarchytype"))
                    .code(resultSet.getString("code"))
                    .identifierId(resultSet.getString("identifierid"))
                    .auditDetails(auditDetails)
                    .build());
        }
        return boundaryIdentifierList;
    }
}
