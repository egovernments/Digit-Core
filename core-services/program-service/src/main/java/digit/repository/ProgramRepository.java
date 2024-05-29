package digit.repository;

import digit.repository.querybuilder.ProgramQueryBuilder;
import digit.repository.rowmapper.ProgramRowMapper;
import digit.web.models.Program;
import digit.web.models.ProgramSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class ProgramRepository {

	@Autowired
	private ProgramQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ProgramRowMapper rowMapper;

	public List<Program> getPrograms(ProgramSearch searchCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getProgramSearchQuery(searchCriteria, preparedStmtList);
		log.info("Final query: " + query);
		return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
	}
}
