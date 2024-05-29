package digit.repository.rowmapper;

import digit.web.models.AdditionalInfo;
import digit.web.models.AuditDetails;
import digit.web.models.Estimate;
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
public class EstimateRowMapper implements ResultSetExtractor<List<Estimate>> {

	public List<Estimate> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, Estimate> estimateMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("eestimateId");
			Estimate estimate = estimateMap.get(id);

			if (estimate == null) {
				Long lastModifiedTime = rs.getLong("elastModifiedTime");
				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				AuditDetails auditDetails = AuditDetails.builder()
						.createdBy(rs.getString("ecreatedBy"))
						.createdTime(rs.getLong("ecreatedTime"))
						.lastModifiedBy(rs.getString("elastModifiedBy"))
						.lastModifiedTime(lastModifiedTime)
						.build();

				Status status = Status.builder()
						.statusCode(Status.StatusCodeEnum.fromValue(rs.getString("estatusCode")))
						.statusMessage(rs.getString("estatusMessage"))
						.build();

				estimate = Estimate.builder()
						.id(rs.getString("eid"))
						.tenantId(rs.getString("etenantId"))
						.programCode(rs.getString("eprogramCode"))
						.projectCode(rs.getString("eprojectCode"))
						.estimateId(rs.getString("eestimateId"))
						.description(rs.getString("edescription"))
						.netAmount(rs.getBigDecimal("enetAmount"))
						.grossAmount(rs.getBigDecimal("egrossAmount"))
						.status(status)
						.additionalDetails(new AdditionalInfo(rs.getString("eadditionalDetails")))
						.auditDetails(auditDetails)
						.build();

				estimateMap.put(id, estimate);
			}
		}

		return new ArrayList<>(estimateMap.values());
	}
}
