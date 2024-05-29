package digit.repository;

import digit.repository.querybuilder.SanctionQueryBuilder;
import digit.repository.rowmapper.SanctionRowMapper;
import digit.web.models.Sanction;
import digit.web.models.SanctionSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class SanctionRepository {

	@Autowired
	private SanctionQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private SanctionRowMapper rowMapper;

	public List<Sanction> getSanctions(SanctionSearch searchCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getSanctionSearchQuery(searchCriteria, preparedStmtList);
		log.info("Final query: " + query);
		return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
	}
}
