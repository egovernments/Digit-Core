package digit.repository;

import digit.repository.querybuilder.AgencyQueryBuilder;
import digit.repository.rowmapper.AgencyRowMapper;
import digit.web.models.Agency;
import digit.web.models.AgencySearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class AgencyRepository {
	@Autowired
	private AgencyQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private AgencyRowMapper rowMapper;

	public List<Agency> getAgencies(AgencySearch searchCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getAgencySearchQuery(searchCriteria, preparedStmtList);
		log.info("Final query: " + query);
		return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
	}
}
