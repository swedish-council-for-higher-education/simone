
package se.uhr.simone.atom.feed.server.entity;

import java.util.*;
import java.util.function.Function;

import se.uhr.simone.atom.feed.server.entity.AtomCategory.Label;
import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;
import se.uhr.simone.atom.feed.utils.jdbc.ResultSetAdapter;
import se.uhr.simone.atom.feed.utils.jdbc.RowMapper;

public class AtomEntryDAO {

	private static final TimeZone UTC_TZ = TimeZone.getTimeZone("UTC");

	static final int MAX_NUM_OF_ENTRIES_TO_RETURN = 10_000;

	private final JdbcTemplate jdbcTemplate;

	public AtomEntryDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean exists(String atomEntryId) {
        return jdbcTemplate.resultsExists("SELECT 1 FROM ATOM_ENTRY WHERE ENTRY_ID=? ", atomEntryId);
	}

	public void insert(AtomEntry atomEntry) {
        jdbcTemplate.update("INSERT INTO ATOM_ENTRY (ENTRY_ID, ENTRY_CONTENT_TYPE, FEED_ID, SORT_ORDER, SUBMITTED, TITLE, ENTRY_XML, SUMMARY, SUMMARY_CONTENT_TYPE) VALUES (?,?,?,?,?,?,?,?,?)", atomEntry.getAtomEntryId(), atomEntry.getContent().map(getContentType()).orElse(null),
				atomEntry.getFeedId(), atomEntry.getSortOrder(), toUTCCalendar(atomEntry.getSubmitted()),
				atomEntry.getTitle(), atomEntry.getContent().map(Content::getValue).orElse(null),
				atomEntry.getSummary().map(Content::getValue).orElse(null), atomEntry.getSummary().map(getContentType()).orElse(null));
	}

	public void update(AtomEntry atomEntry) {
        jdbcTemplate.update("UPDATE ATOM_ENTRY SET FEED_ID=?, SUBMITTED=?, TITLE=?, ENTRY_XML=?, ENTRY_CONTENT_TYPE=? WHERE ENTRY_ID=? ", atomEntry.getFeedId(), toUTCCalendar(atomEntry.getSubmitted()),
				atomEntry.getTitle(), atomEntry.getContent().map(Content::getValue).orElse(null),
				atomEntry.getContent().map(getContentType()).orElse(null), atomEntry.getAtomEntryId());
	}

	private Function<? super Content, ? extends String> getContentType() {
		return contentType -> contentType.getContentType().orElse(null);
	}

	public Optional<AtomEntry> fetchBy(String atomEntryId) {
        return jdbcTemplate.queryForObject("SELECT SORT_ORDER, ENTRY_ID, ENTRY_CONTENT_TYPE, FEED_ID, SUBMITTED, TITLE, ENTRY_XML, SUMMARY, SUMMARY_CONTENT_TYPE FROM ATOM_ENTRY WHERE ENTRY_ID = ? ", new AtomEntryRowMapper(), atomEntryId);
	}

	public List<AtomEntry> getAtomEntriesForFeed(long id) {
        return jdbcTemplate.query("SELECT SORT_ORDER, ENTRY_ID, ENTRY_CONTENT_TYPE, FEED_ID, SUBMITTED, TITLE, ENTRY_XML, SUMMARY, SUMMARY_CONTENT_TYPE FROM ATOM_ENTRY WHERE FEED_ID = ? ORDER BY SORT_ORDER DESC, SUBMITTED DESC", new AtomEntryRowMapper(), id);
	}

	public Optional<String> getLatestEntryIdForCategory(AtomCategory category) {
        String sql = "SELECT " +
                "SELECT " +
                "AE.ENTRY_ID " +
                "FROM ATOM_ENTRY AE " +
                "inner join ATOM_CATEGORY AC on AE.ENTRY_ID = AC.ENTRY_ID " +
                "WHERE AC.TERM = ? and AC.LABEL = ? " +
                "ORDER BY SORT_ORDER DESC, SUBMITTED DESC FETCH FIRST 1 ROWS ONLY ";

		return jdbcTemplate.queryForObject(sql, new EntryIdRowmapper(), category.getTerm().getValue(),
				category.getLabel().map(Label::getValue).orElse(null));
	}

	public List<AtomEntry> getEntriesNotConnectedToFeed() {
        String sql = "SELECT SORT_ORDER, ENTRY_ID, ENTRY_CONTENT_TYPE, FEED_ID, SUBMITTED, TITLE, ENTRY_XML, SUMMARY, SUMMARY_CONTENT_TYPE FROM ATOM_ENTRY WHERE FEED_ID IS NULL ORDER BY SORT_ORDER ASC, SUBMITTED ASC FETCH FIRST "
                + MAX_NUM_OF_ENTRIES_TO_RETURN + " ROWS ONLY";
		return jdbcTemplate.query(sql, new AtomEntryRowMapper());
	}

	private static class EntryIdRowmapper implements RowMapper<String> {

		@Override
		public String mapRow(ResultSetAdapter rs) {
			return rs.getString("ENTRY_ID");
		}
	}

	/**
	 * Convert date to UTC
	 *
	 * @param date The date to convert
	 * @return The timestamp in UTC.
	 */

	private static Calendar toUTCCalendar(Date date) {
		Calendar c = Calendar.getInstance(UTC_TZ);
		c.setTime(date);
		return c;
	}
}