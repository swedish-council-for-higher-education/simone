package se.uhr.simone.core.entity;

import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;

import java.io.InputStream;


public class SqlScriptRunner {

	private final JdbcTemplate jdbcTemplate;

	public SqlScriptRunner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void execute(InputStream is) {
		SqlScriptReader reader = new SqlScriptReader(is);

		for (String stmt : reader.getStatements()) {
			jdbcTemplate.execute(stmt);
		}
	}
}
