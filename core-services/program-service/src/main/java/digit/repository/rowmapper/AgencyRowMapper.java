package digit.repository.rowmapper;

import digit.web.models.Agency;
import digit.web.models.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgencyRowMapper implements ResultSetExtractor<List<Agency>> {

	public List<Agency> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, Agency> agencyMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("aagencyId");
			Agency agency = agencyMap.get(id);

			if (agency == null) {
				Long lastModifiedTime = rs.getLong("alastModifiedTime");
				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				AuditDetails auditDetails = AuditDetails.builder()
						.createdBy(rs.getString("acreatedBy"))
						.createdTime(rs.getLong("acreatedTime"))
						.lastModifiedBy(rs.getString("alastModifiedBy"))
						.lastModifiedTime(lastModifiedTime)
						.build();

				agency = Agency.builder()
						.id(rs.getString("aid"))
						.tenantId(rs.getString("atenantId"))
						.agencyId(rs.getString("aagencyId"))
						.agencyType(rs.getString("aagencyType"))
						.programCode(rs.getString("aprogramCode"))
						.orgNumber(rs.getString("aorgNumber"))
						.auditDetails(auditDetails)
						.build();

				agencyMap.put(id, agency);
			}
		}

		return new ArrayList<>(agencyMap.values());
	}
}
