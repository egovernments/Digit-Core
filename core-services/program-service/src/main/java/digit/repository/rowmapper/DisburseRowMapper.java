package digit.repository.rowmapper;

import digit.web.models.AdditionalInfo;
import digit.web.models.AuditDetails;
import digit.web.models.Disburse;
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
public class DisburseRowMapper implements ResultSetExtractor<List<Disburse>> {

	public List<Disburse> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, Disburse> disburseMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("ddisburseId");
			Disburse disburse = disburseMap.get(id);

			if (disburse == null) {
				Long lastModifiedTime = rs.getLong("dlastModifiedTime");
				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				AuditDetails auditDetails = AuditDetails.builder()
						.createdBy(rs.getString("dcreatedBy"))
						.createdTime(rs.getLong("dcreatedTime"))
						.lastModifiedBy(rs.getString("dlastModifiedBy"))
						.lastModifiedTime(lastModifiedTime)
						.build();

				Status status = Status.builder()
						.statusCode(Status.StatusCodeEnum.fromValue(rs.getString("dstatusCode")))
						.statusMessage(rs.getString("dstatusMessage"))
						.build();

				disburse = Disburse.builder()
						.id(rs.getString("did"))
						.tenantId(rs.getString("dtenantId"))
						.programCode(rs.getString("dprogramCode"))
						.projectCode(rs.getString("dprojectCode"))
						.disburseId(rs.getString("ddisburseId"))
						.targetId(rs.getString("dtargetId"))
						.transactionId(rs.getString("dtransactionId"))
						.sanctionId(rs.getString("dsanctionId"))
						.amountCode(rs.getString("damountCode"))
						.netAmount(rs.getBigDecimal("dnetAmount"))
						.grossAmount(rs.getBigDecimal("dgrossamount"))
						.status(status)
						.additionalDetails(new AdditionalInfo(rs.getString("dadditionalDetails")))
						.auditDetails(auditDetails)
						.build();

				disburseMap.put(id, disburse);
			}
		}

		return new ArrayList<>(disburseMap.values());
	}
}
