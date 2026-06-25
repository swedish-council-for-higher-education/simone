package se.uhr.simone.atom.feed.server.entity;

import java.util.List;
import java.util.Objects;

import se.uhr.simone.atom.feed.server.entity.AtomCategory.Build;
import se.uhr.simone.atom.feed.server.entity.AtomCategory.Label;
import se.uhr.simone.atom.feed.server.entity.AtomCategory.Term;
import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;
import se.uhr.simone.atom.feed.utils.jdbc.ResultSetAdapter;
import se.uhr.simone.atom.feed.utils.jdbc.RowMapper;

public class AtomCategoryDAO {

	private final JdbcTemplate jdbcTemplate;

	public AtomCategoryDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean isConnected(AtomCategory atomCategory, String atomEntryId) {
        return jdbcTemplate.resultsExists("SELECT 1 FROM ATOM_CATEGORY WHERE TERM=? AND ENTRY_ID=?", atomCategory.getTerm().getValue(), atomEntryId);
	}

	public void connectEntryToCategory(String atomEntryId, AtomCategory atomCategory) {
        jdbcTemplate.update("INSERT INTO ATOM_CATEGORY (ENTRY_ID, TERM, LABEL) VALUES (?,?,?)", atomEntryId, atomCategory.getTerm().getValue(),
				atomCategory.getLabel().map(Label::getValue).orElse(null));
	}

	public List<AtomCategory> getCategoriesForAtomEntry(String atomEntryId) {
        return jdbcTemplate.query("SELECT TERM, LABEL FROM ATOM_CATEGORY WHERE ENTRY_ID=? ", new AtomCategoryRowMapper(), atomEntryId);
	}

	private static class AtomCategoryRowMapper implements RowMapper<AtomCategory> {

		@Override
		public AtomCategory mapRow(ResultSetAdapter rs) {
			String labelValue = rs.getString("LABEL");
			Build builder = AtomCategory.builder().withTerm(Term.of(rs.getString("TERM")));
			if (Objects.nonNull(labelValue)) {
				builder.withLabel(Label.of(labelValue));
			}
			return builder.build();
		}
	}
}