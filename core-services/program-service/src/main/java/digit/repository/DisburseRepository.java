package digit.repository;

import digit.repository.querybuilder.DisburseQueryBuilder;
import digit.repository.rowmapper.DisburseRowMapper;
import digit.web.models.Disburse;
import digit.web.models.DisburseSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class DisburseRepository {

	@Autowired
	private DisburseQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DisburseRowMapper rowMapper;

	public List<Disburse> getDisburses(DisburseSearch searchCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getDisburseSearchQuery(searchCriteria, preparedStmtList);
		log.info("Final query: " + query);
		return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
	}
}
