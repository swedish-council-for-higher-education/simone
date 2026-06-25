package se.uhr.simone.atom.feed.server.entity;

import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;
import se.uhr.simone.atom.feed.utils.jdbc.ResultSetAdapter;
import se.uhr.simone.atom.feed.utils.jdbc.RowMapper;

import java.util.List;

public class AtomAuthorDAO {

	private final JdbcTemplate jdbcTemplate;

	public AtomAuthorDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean exists(String atomEntryId) {
        return jdbcTemplate.resultsExists("SELECT 1 FROM ATOM_AUTHOR WHERE ENTRY_ID = ?", atomEntryId);
	}

	public void insert(String id, Person person) {
        jdbcTemplate.update("INSERT INTO ATOM_AUTHOR (ENTRY_ID, AUTHOR) VALUES (?,?)", id, person.getName());
	}

	public void delete(String atomEntryId) {
        jdbcTemplate.update("DELETE FROM ATOM_AUTHOR WHERE ENTRY_ID = ? ", atomEntryId);
	}

	public List<Person> findBy(String atomEntryId) {
        return jdbcTemplate.query("SELECT AUTHOR FROM ATOM_AUTHOR WHERE ENTRY_ID = ? ", new AtomAuthorRowMapper(), atomEntryId);
	}

	private static class AtomAuthorRowMapper implements RowMapper<Person> {

		@Override
		public Person mapRow(ResultSetAdapter rs) {
			return Person.of(rs.getString("AUTHOR"));
		}
	}
}
