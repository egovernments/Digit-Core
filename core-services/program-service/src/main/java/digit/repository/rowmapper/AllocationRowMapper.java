package digit.repository.rowmapper;

import digit.web.models.AdditionalInfo;
import digit.web.models.Allocation;
import digit.web.models.AuditDetails;
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
public class AllocationRowMapper implements ResultSetExtractor<List<Allocation>> {

	public List<Allocation> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, Allocation> allocationMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("aallocationId");
			Allocation allocation = allocationMap.get(id);

			if (allocation == null) {

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

				Status status = Status.builder()
						.statusCode(Status.StatusCodeEnum.fromValue(rs.getString("astatusCode")))
						.statusMessage(rs.getString("astatusMessage"))
						.build();

				allocation = Allocation.builder()
						.id(rs.getString("aid"))
						.tenantId(rs.getString("atenantId"))
						.programCode(rs.getString("aprogramCode"))
						.projectCode(rs.getString("aprojectCode"))
						.sanctionId(rs.getString("asanctionId"))
						.allocationId(rs.getString("aallocationId"))
						.netAmount(rs.getBigDecimal("anetAmount"))
						.grossAmount(rs.getBigDecimal("agrossAmount"))
						.allocationType(Allocation.AllocationTypeEnum.fromValue(rs.getString("aallocationType")))
						.status(status)
						.additionalDetails(new AdditionalInfo(rs.getString("aadditionalDetails")))
						.auditDetails(auditDetails)
						.build();

				allocationMap.put(id, allocation);
			}
		}

		return new ArrayList<>(allocationMap.values());
	}
}
