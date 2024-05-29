package digit.repository;

import digit.repository.querybuilder.EstimateQueryBuilder;
import digit.repository.rowmapper.EstimateRowMapper;
import digit.web.models.Estimate;
import digit.web.models.EstimateSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class EstimateRepository {

	@Autowired
	private EstimateQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EstimateRowMapper rowMapper;

	public List<Estimate> getEstimates(EstimateSearch searchCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getEstimateSearchQuery(searchCriteria, preparedStmtList);
		log.info("Final query: " + query);
		return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
	}
}
