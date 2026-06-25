package se.uhr.simone.atom.feed.server.entity;

import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;
import se.uhr.simone.atom.feed.utils.jdbc.ResultSetAdapter;
import se.uhr.simone.atom.feed.utils.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AtomLinkDAO {

	static final int MAX_NUM_OF_ENTRIES_TO_RETURN = 10_000;

	private JdbcTemplate jdbcTemplate;

	public AtomLinkDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean exists(String atomEntryId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT 1 FROM ATOM_LINK WHERE ENTRY_ID = ?");
		return jdbcTemplate.resultsExists(sql.toString(), atomEntryId);
	}

	public void insert(String id, AtomLink atomLink) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ATOM_LINK (ENTRY_ID, REL, HREF, CONTENT_TYPE) VALUES (?,?,?,?)");
		jdbcTemplate.update(sql.toString(), id, atomLink.getRel(), atomLink.getHref(), atomLink.getType());
	}

	public void delete(String atomEntryId) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ATOM_LINK WHERE ENTRY_ID = ? ");
		jdbcTemplate.update(sql.toString(), atomEntryId);
	}

	public List<AtomLink> findBy(String atomEntryId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT REL, HREF, CONTENT_TYPE FROM ATOM_LINK WHERE ENTRY_ID = ? ");
		return jdbcTemplate.query(sql.toString(), new AtomLinkRowMapper(), atomEntryId);
	}

	private static class AtomLinkRowMapper implements RowMapper<AtomLink> {

		@Override
		public AtomLink mapRow(ResultSetAdapter rs) {
			return AtomLink.builder() //
					.withRel(rs.getString("REL"))
					.withHref(rs.getString("HREF")) //
					.withType(rs.getString("CONTENT_TYPE")) //
					.build();
		}
	}
}
