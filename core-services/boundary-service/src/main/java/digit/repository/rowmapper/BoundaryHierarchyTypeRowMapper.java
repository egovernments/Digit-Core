package digit.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.BoundaryTypeHierarchy;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BoundaryHierarchyTypeRowMapper implements ResultSetExtractor<List<BoundaryTypeHierarchyDefinition>> {

    private ObjectMapper objectMapper;

    public BoundaryHierarchyTypeRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<BoundaryTypeHierarchyDefinition> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinitionList = new ArrayList<>();

        while(resultSet.next()) {
            AuditDetails auditDetails = AuditDetails.builder()
                    .createdBy(resultSet.getString("createdby"))
                    .createdTime(resultSet.getLong("createdtime"))
                    .lastModifiedBy(resultSet.getString("lastmodifiedby"))
                    .lastModifiedTime(resultSet.getLong("lastmodifiedtime"))
                    .build();

            List<BoundaryTypeHierarchy> boundaryTypeHierarchyList = getBoundaryHierarchyList(((PGobject) resultSet.getObject("boundaryhierarchy")).getValue());

            BoundaryTypeHierarchyDefinition boundaryTypeHierarchyDefinition = BoundaryTypeHierarchyDefinition.builder()
                    .id(resultSet.getString("id"))
                    .tenantId(resultSet.getString("tenantid"))
                    .hierarchyType(resultSet.getString("hierarchytype"))
                    .boundaryHierarchy(boundaryTypeHierarchyList)
                    .auditDetails(auditDetails)
                    .build();

            boundaryTypeHierarchyDefinitionList.add(boundaryTypeHierarchyDefinition);
        }

        return boundaryTypeHierarchyDefinitionList;
    }

    private List<BoundaryTypeHierarchy> getBoundaryHierarchyList(String boundaryHierarchyJsonString) {
        List<BoundaryTypeHierarchy> boundaryHierarchyList;

        try {
            boundaryHierarchyList = objectMapper.readValue(boundaryHierarchyJsonString, new TypeReference<List<BoundaryTypeHierarchy>>() {});
        } catch (Exception e) {
            throw new CustomException("PARSING_ERR", "Could not parse boundary type hierarchy from PSQL result set.");
        }

        return boundaryHierarchyList;
    }
}
