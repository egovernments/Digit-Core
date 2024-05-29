package digit.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.AdditionalInfo;
import digit.web.models.AuditDetails;
import digit.web.models.Program;
import digit.web.models.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class ProgramRowMapper implements ResultSetExtractor<List<Program>> {

	@Autowired
	private ObjectMapper objectMapper;

	public List<Program> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, Program> programMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("pprogramId");
			Program program = programMap.get(id);

			if (program == null) {
				// Retrieve JSONB data for objective and criteria
				String objectiveJson = rs.getString("pobjective");
				String criteriaJson = rs.getString("pcriteria");

				// Parse JSON arrays into lists of strings
				List<String> objectives = parseJsonArray(objectMapper, objectiveJson);
				List<String> criteria = parseJsonArray(objectMapper, criteriaJson);

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

				program = Program.builder()
						.id(rs.getString("pid"))
						.tenantId(rs.getString("ptenantId"))
						.programCode(rs.getString("pprogramCode"))
						.programId(rs.getString("pprogramId"))
						.name(rs.getString("pname"))
						.description(rs.getString("pdescription"))
						.startDate(rs.getBigDecimal("pstartDate"))
						.endDate(rs.getBigDecimal("pendDate"))
						.status(status)
						.additionalDetails(new AdditionalInfo(rs.getString("padditionalDetails")))
						.auditDetails(auditDetails)
						.objective(objectives) // Initialize empty list
						.criteria(criteria) // Initialize empty list
						.build();

				programMap.put(id, program);
			}
		}

		return new ArrayList<>(programMap.values());
	}

	// Helper method to parse JSON array into list of strings
	private List<String> parseJsonArray(ObjectMapper objectMapper, String jsonArray) {
		if (jsonArray == null) {
			return Collections.emptyList();
		}

		try {
			return objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
		} catch (IOException e) {
			// Handle parsing exception
			return Collections.emptyList();
		}
	}
}
