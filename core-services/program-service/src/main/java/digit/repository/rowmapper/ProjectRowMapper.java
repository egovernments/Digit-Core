package digit.repository.rowmapper;

import digit.web.models.AdditionalInfo;
import digit.web.models.AuditDetails;
import digit.web.models.Project;
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
public class ProjectRowMapper implements ResultSetExtractor<List<Project>> {

	public List<Project> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, Project> projectMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("pprojectId");
			Project project = projectMap.get(id);

			if (project == null) {
				Long lastModifiedTime = rs.getLong("plastModifiedTime");
				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				AuditDetails auditDetails = AuditDetails.builder()
						.createdBy(rs.getString("pcreatedBy"))
						.createdTime(rs.getLong("pcreatedTime"))
						.lastModifiedBy(rs.getString("plastModifiedBy"))
						.lastModifiedTime(lastModifiedTime)
						.build();

				Status status = Status.builder()
						.statusCode(Status.StatusCodeEnum.fromValue(rs.getString("pstatusCode")))
						.statusMessage(rs.getString("pstatusMessage"))
						.build();

				project = Project.builder()
						.id(rs.getString("pid"))
						.tenantId(rs.getString("ptenantId"))
						.programCode(rs.getString("pprogramCode"))
						.agencyId(rs.getString("pagencyId"))
						.projectCode(rs.getString("pprojectCode"))
						.projectId(rs.getString("pprojectId"))
						.name(rs.getString("pname"))
						.description(rs.getString("pdescription"))
						.status(status)
						.additionalDetails(new AdditionalInfo(rs.getString("padditionalDetails")))
						.auditDetails(auditDetails)
						.build();

				projectMap.put(id, project);
			}
		}

		return new ArrayList<>(projectMap.values());
	}
}
