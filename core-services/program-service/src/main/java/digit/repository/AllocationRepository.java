package digit.repository;

import digit.repository.querybuilder.AllocationQueryBuilder;
import digit.repository.rowmapper.AllocationRowMapper;
import digit.web.models.Allocation;
import digit.web.models.AllocationSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class AllocationRepository {

	@Autowired
	private AllocationQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private AllocationRowMapper rowMapper;

	public List<Allocation> getAllocations(AllocationSearch searchCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getAllocationSearchQuery(searchCriteria, preparedStmtList);
		log.info("Final query: " + query);
		return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
	}
}
