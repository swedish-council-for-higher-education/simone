package se.uhr.simone.core.feed.entity;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import se.uhr.simone.atom.feed.server.entity.AbstractFeedRepository;
import se.uhr.simone.core.entity.SqlScriptRunner;

public class SimoneFeedRepository extends AbstractFeedRepository {

	private static final Logger LOG = LoggerFactory.getLogger(SimoneFeedRepository.class);

	private final DataSource dataSource;

	public SimoneFeedRepository(DataSource dataSource) {
		super(dataSource);
		this.dataSource = dataSource;
	}

	public Long getNextSortOrder() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		String sql = "SELECT COALESCE(MAX(SORT_ORDER),0) FROM ATOM_ENTRY";

		return jdbcTemplate.queryForObject(sql, Long.class) + 1L;
	}

	@Override
	public void clear() {
		LOG.info("delete all tables");
		SqlScriptRunner runner = new SqlScriptRunner(new JdbcTemplate(dataSource));
		runner.execute(this.getClass().getResourceAsStream("/db/delete_all_tables.sql"));
	}
}
