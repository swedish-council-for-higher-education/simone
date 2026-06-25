package se.uhr.simone.atom.feed.server.entity;

import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;
import se.uhr.simone.atom.feed.utils.jdbc.ResultSetAdapter;
import se.uhr.simone.atom.feed.utils.jdbc.RowMapper;

import java.util.List;
import java.util.Optional;


public class AtomFeedDAO {

	private final JdbcTemplate jdbcTemplate;

	public AtomFeedDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean exists(long id) {
        return jdbcTemplate.resultsExists("SELECT 1 FROM ATOM_FEED WHERE FEED_ID=?", id);
	}

	public void insert(AtomFeed atomFeed) {
        jdbcTemplate.update("INSERT INTO ATOM_FEED (FEED_ID, NEXT_FEED_ID, PREV_FEED_ID, FEED_XML) VALUES (?,?,?,?)", atomFeed.getId(), atomFeed.getNextFeedId(), atomFeed.getPreviousFeedId(),
				atomFeed.getXml());
	}

	public int update(AtomFeed atomFeed) {
        return jdbcTemplate.update("UPDATE ATOM_FEED SET NEXT_FEED_ID=?, PREV_FEED_ID=?, FEED_XML=? WHERE FEED_ID=?", atomFeed.getNextFeedId(), atomFeed.getPreviousFeedId(), atomFeed.getXml(),
				atomFeed.getId());
	}

	public Optional<AtomFeed> fetchBy(long id) {
        return jdbcTemplate.queryForObject("SELECT FEED_ID, NEXT_FEED_ID, PREV_FEED_ID, FEED_XML AS FEED_XML FROM ATOM_FEED WHERE FEED_ID=?", new AtomFeedRowMapper(), id);
	}

	public Optional<AtomFeed> fetchRecent() {
        return jdbcTemplate.queryForObject("SELECT F.FEED_ID, F.NEXT_FEED_ID, F.PREV_FEED_ID, F.FEED_XML AS FEED_XML FROM ATOM_FEED F ORDER BY F.FEED_ID DESC FETCH FIRST 1 ROWS ONLY", new AtomFeedRowMapper());
	}

	public List<AtomFeed> getFeedsWithoutXml() {
        return jdbcTemplate.query("SELECT F.FEED_ID, F.NEXT_FEED_ID, F.PREV_FEED_ID, CAST(NULL as CHAR) AS FEED_XML FROM ATOM_FEED F WHERE F.FEED_XML_IS_NULL = 1 AND F.NEXT_FEED_ID IS NOT NULL", new AtomFeedRowMapper());
	}

	public int saveAtomFeedXml(long feedId, String xml) {
        return jdbcTemplate.update("UPDATE ATOM_FEED SET FEED_XML=? WHERE FEED_ID=?", xml, feedId);
	}

	protected static class AtomFeedRowMapper implements RowMapper<AtomFeed> {

		public AtomFeedRowMapper() {
		}

		@Override
		public AtomFeed mapRow(ResultSetAdapter rs) {
			AtomFeed atomFeed = new AtomFeed(rs.getLong("FEED_ID"));
			long NEXT_FEED_ID = rs.getLong("NEXT_FEED_ID");
			if (rs.wasNull()) {
				atomFeed.setNextFeedId(null);
			} else {
				atomFeed.setNextFeedId(NEXT_FEED_ID);
			}

			long previousFeedId = rs.getLong("PREV_FEED_ID");
			if (rs.wasNull()) {
				atomFeed.setPreviousFeedId(null);
			} else {
				atomFeed.setPreviousFeedId(previousFeedId);
			}
			atomFeed.setXml(rs.getString("FEED_XML"));
			return atomFeed;
		}
	}
}
