package se.uhr.simone.atom.feed.server.entity;

import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;

import java.util.List;

public interface FeedRepository {

	void clear();

	AtomFeedDAO createAtomFeedDAO(JdbcTemplate jdbcTemplate);

	AtomEntryDAO createAtomEntryDAO(JdbcTemplate jdbcTemplate);

	AtomCategoryDAO createAtomCategoryDAO(JdbcTemplate jdbcTemplate);

	AtomLinkDAO createAtomLinkDAO(JdbcTemplate jdbcTemplate);

	AtomAuthorDAO createAtomAuthorDAO(JdbcTemplate jdbcTemplate);

	void saveAtomFeed(AtomFeed atomFeed);

	void saveAtomEntry(AtomEntry atomEntry);

	void saveAtomFeedXml(long feedId, String xml);

	AtomFeed getFeedById(long id);

	AtomFeed getRecentFeed();

	List<AtomEntry> getEntriesNotConnectedToFeed();

	List<AtomFeed> getFeedsWithoutXml();

	String getLatestEntryIdForCategory(AtomCategory category);
}
