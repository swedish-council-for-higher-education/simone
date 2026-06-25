package se.uhr.simone.atom.feed.server.entity;

import se.uhr.simone.atom.feed.utils.jdbc.ResultSetAdapter;
import se.uhr.simone.atom.feed.utils.jdbc.RowMapper;

class AtomEntryRowMapper implements RowMapper<AtomEntry> {

	@Override
	public AtomEntry mapRow(ResultSetAdapter rs) {

		Long feedId = rs.getLong("FEED_ID");
		if (rs.wasNull()) {
			feedId = null;
		}

		return AtomEntry.builder() //
				.withAtomEntryId(rs.getString("ENTRY_ID")) //
				.withSortOrder(rs.getLong("SORT_ORDER"))
				.withSubmitted(rs.getTimestamp("SUBMITTED")) // ,
				.withFeedId(feedId) //
				.withTitle(rs.getString("TITLE")) //
				.withContent(Content.builder()
						.withValue(rs.getString("ENTRY_XML"))
						.withContentType(rs.getString("ENTRY_CONTENT_TYPE"))
						.build())
				.withSummary(Content.builder()
						.withValue(rs.getString("SUMMARY"))
						.withContentType(rs.getString("SUMMARY_CONTENT_TYPE"))
						.build())
				.build();

	}
}