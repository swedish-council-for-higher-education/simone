package se.uhr.simone.atom.feed.server.entity;

import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;
import se.uhr.simone.atom.feed.utils.jdbc.ResultSetAdapter;
import se.uhr.simone.atom.feed.utils.jdbc.RowMapper;

import java.util.List;
import java.util.Optional;


public class AtomFeedDAO {

	private JdbcTemplate jdbcTemplate;

	public AtomFeedDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean exists(long id) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT 1 FROM ATOM_FEED WHERE FEED_ID=?");
		return jdbcTemplate.resultsExists(sql.toString(), id);
	}

	public void insert(AtomFeed atomFeed) {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"INSERT INTO ATOM_FEED (FEED_ID, NEXT_FEED_ID, PREV_FEED_ID, FEED_XML) VALUES (?,?,?,?)");
		jdbcTemplate.update(sql.toString(), atomFeed.getId(), atomFeed.getNextFeedId(), atomFeed.getPreviousFeedId(),
				atomFeed.getXml());
	}

	public int update(AtomFeed atomFeed) {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"UPDATE ATOM_FEED SET NEXT_FEED_ID=?, PREV_FEED_ID=?, FEED_XML=? WHERE FEED_ID=?");
		return jdbcTemplate.update(sql.toString(), atomFeed.getNextFeedId(), atomFeed.getPreviousFeedId(), atomFeed.getXml(),
				atomFeed.getId());
	}

	public Optional<AtomFeed> fetchBy(long id) {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT FEED_ID, NEXT_FEED_ID, PREV_FEED_ID, FEED_XML AS FEED_XML FROM ATOM_FEED WHERE FEED_ID=?");
		return jdbcTemplate.queryForObject(sql.toString(), new AtomFeedRowMapper(), id);
	}

	public Optional<AtomFeed> fetchRecent() {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT F.FEED_ID, F.NEXT_FEED_ID, F.PREV_FEED_ID, F.FEED_XML AS FEED_XML FROM ATOM_FEED F ORDER BY F.FEED_ID DESC FETCH FIRST 1 ROWS ONLY");
		return jdbcTemplate.queryForObject(sql.toString(), new AtomFeedRowMapper());
	}

	public List<AtomFeed> getFeedsWithoutXml() {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT F.FEED_ID, F.NEXT_FEED_ID, F.PREV_FEED_ID, CAST(NULL as CHAR) AS FEED_XML FROM ATOM_FEED F WHERE F.FEED_XML_IS_NULL = 1 AND F.NEXT_FEED_ID IS NOT NULL");
		return jdbcTemplate.query(sql.toString(), new AtomFeedRowMapper());
	}

	public int saveAtomFeedXml(long feedId, String xml) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ATOM_FEED SET FEED_XML=? WHERE FEED_ID=?");
		return jdbcTemplate.update(sql.toString(), xml, feedId);
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
