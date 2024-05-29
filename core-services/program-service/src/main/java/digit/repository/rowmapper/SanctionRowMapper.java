package digit.repository.rowmapper;

import digit.web.models.AdditionalInfo;
import digit.web.models.AuditDetails;
import digit.web.models.Sanction;
import digit.web.models.Status;
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
public class SanctionRowMapper implements ResultSetExtractor<List<Sanction>> {

	public List<Sanction> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, Sanction> sanctionMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("ssanctionId");
			Sanction sanction = sanctionMap.get(id);

			if (sanction == null) {
				Long lastModifiedTime = rs.getLong("slastModifiedTime");
				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				AuditDetails auditDetails = AuditDetails.builder()
						.createdBy(rs.getString("screatedBy"))
						.createdTime(rs.getLong("screatedTime"))
						.lastModifiedBy(rs.getString("slastModifiedBy"))
						.lastModifiedTime(lastModifiedTime)
						.build();

				Status status = Status.builder()
						.statusCode(Status.StatusCodeEnum.fromValue(rs.getString("sstatusCode")))
						.statusMessage(rs.getString("sstatusMessage"))
						.build();

				sanction = Sanction.builder()
						.id(rs.getString("sid"))
						.tenantId(rs.getString("stenantId"))
						.programCode(rs.getString("sprogramCode"))
						.projectCode(rs.getString("sprojectCode"))
						.sanctionId(rs.getString("ssanctionId"))
						.netAmount(rs.getBigDecimal("snetAmount"))
						.grossAmount(rs.getBigDecimal("sgrossAmount"))
						.availableAmount(rs.getBigDecimal("savailableAmount"))
						.status(status)
						.additionalDetails(new AdditionalInfo(rs.getString("sadditionalDetails")))
						.auditDetails(auditDetails)
						.build();

				sanctionMap.put(id, sanction);
			}
		}

		return new ArrayList<>(sanctionMap.values());
	}
}
