package se.uhr.simone.atom.feed.server.entity;

import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;

import java.util.List;

import javax.sql.DataSource;

public abstract class AbstractFeedRepository implements FeedRepository {

	private final AtomFeedDAO atomFeedDAO;
	private final AtomEntryDAO atomEntryDAO;
	private final AtomCategoryDAO atomCategoryDAO;
	private final AtomLinkDAO atomLinkDAO;
	private final AtomAuthorDAO atomAuthorDAO;

	protected AbstractFeedRepository(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		atomFeedDAO = createAtomFeedDAO(jdbcTemplate);
		atomEntryDAO = createAtomEntryDAO(jdbcTemplate);
		atomCategoryDAO = createAtomCategoryDAO(jdbcTemplate);
		atomLinkDAO = createAtomLinkDAO(jdbcTemplate);
		atomAuthorDAO = createAtomAuthorDAO(jdbcTemplate);
	}

	AbstractFeedRepository(AtomFeedDAO atomFeedDAO, AtomEntryDAO atomEntryDAO, AtomCategoryDAO atomCategoryDAO, AtomLinkDAO atomLinkDAO, AtomAuthorDAO atomAuthorDAO) {
		this.atomFeedDAO = atomFeedDAO;
		this.atomEntryDAO = atomEntryDAO;
		this.atomCategoryDAO = atomCategoryDAO;
		this.atomLinkDAO = atomLinkDAO;
		this.atomAuthorDAO = atomAuthorDAO;
	}

	@Override
	public AtomFeedDAO createAtomFeedDAO(JdbcTemplate jdbcTemplate) {
		return new AtomFeedDAO(jdbcTemplate);
	}

	@Override
	public AtomEntryDAO createAtomEntryDAO(JdbcTemplate jdbcTemplate) {
		return new AtomEntryDAO(jdbcTemplate);
	}

	@Override
	public AtomCategoryDAO createAtomCategoryDAO(JdbcTemplate jdbcTemplate) {
		return new AtomCategoryDAO(jdbcTemplate);
	}

	@Override
	public AtomLinkDAO createAtomLinkDAO(JdbcTemplate jdbcTemplate) {
		return new AtomLinkDAO(jdbcTemplate);
	}

	@Override
	public AtomAuthorDAO createAtomAuthorDAO(JdbcTemplate jdbcTemplate) {
		return new AtomAuthorDAO(jdbcTemplate);
	}

	@Override
	public void saveAtomFeed(AtomFeed atomFeed) {
		if (atomFeedDAO.exists(atomFeed.getId())) {
			atomFeedDAO.update(atomFeed);
		} else {
			atomFeedDAO.insert(atomFeed);
		}

		for (AtomEntry atomEntry : atomFeed.getEntries()) {
			atomEntry.setFeedId(atomFeed.getId());
			saveAtomEntry(atomEntry);
		}
	}

	@Override
	public void saveAtomEntry(AtomEntry atomEntry) {
		if (atomEntryDAO.exists(atomEntry.getAtomEntryId())) {
			atomEntryDAO.update(atomEntry);
			atomLinkDAO.delete(atomEntry.getAtomEntryId());
			atomAuthorDAO.delete(atomEntry.getAtomEntryId());
		} else {
			atomEntryDAO.insert(atomEntry);
		}

		for (AtomCategory atomCategory : atomEntry.getAtomCategories()) {
			if (!atomCategoryDAO.isConnected(atomCategory, atomEntry.getAtomEntryId())) {
				atomCategoryDAO.connectEntryToCategory(atomEntry.getAtomEntryId(), atomCategory);
			}
		}

		for (AtomLink atomLink : atomEntry.getAtomLinks()) {
			atomLinkDAO.insert(atomEntry.getAtomEntryId(), atomLink);
		}

		for (Person author : atomEntry.getAuthors()) {
			atomAuthorDAO.insert(atomEntry.getAtomEntryId(), author);
		}

	}

	@Override
	public void saveAtomFeedXml(long feedId, String xml) {
		atomFeedDAO.saveAtomFeedXml(feedId, xml);
	}

	@Override
	public AtomFeed getFeedById(long id) {
		AtomFeed atomFeed = atomFeedDAO.fetchBy(id).orElse(null);

		if(atomFeed != null){
			atomFeed.setEntries(getEntriesForFeed(atomFeed));
		}

		return atomFeed;
	}

	/**
	 * Will return the "recent" {@link AtomFeed} or null if not existing.
	 * 
	 * @return "recent" {@link AtomFeed} or null if not existing.
	 */
	@Override
	public AtomFeed getRecentFeed() {
		var recent = atomFeedDAO.fetchRecent().orElse(null);

		if(recent != null){
			recent.setEntries(getEntriesForFeed(recent));
		}
		return recent;
	}

	/**
	 * Will return all {@link AtomEntry}s that are not connected to a {@link AtomFeed}.
	 * 
	 * @return all {@link AtomEntry}s that are not connected to a {@link AtomFeed}
	 */
	@Override
	public List<AtomEntry> getEntriesNotConnectedToFeed() {
		List<AtomEntry> entriesNotConnectedToFeed = atomEntryDAO.getEntriesNotConnectedToFeed();
		for (AtomEntry atomEntry : entriesNotConnectedToFeed) {
			atomEntry.setAtomCategories(atomCategoryDAO.getCategoriesForAtomEntry(atomEntry.getAtomEntryId()));
			atomEntry.setAtomLink(atomLinkDAO.findBy(atomEntry.getAtomEntryId()));
			atomEntry.setAuthors(atomAuthorDAO.findBy(atomEntry.getAtomEntryId()));
		}
		return entriesNotConnectedToFeed;
	}

	/**
	 * Will return a list of {@link AtomFeed}s that has no xml.
	 * 
	 * @return all {@link AtomFeed}s that has no xml.
	 */
	@Override
	public List<AtomFeed> getFeedsWithoutXml() {
		List<AtomFeed> feedsWithoutXml = atomFeedDAO.getFeedsWithoutXml();
		for (AtomFeed atomFeed : feedsWithoutXml) {
			atomFeed.setEntries(getEntriesForFeed(atomFeed));
		}
		return feedsWithoutXml;
	}

	@Override
	public String getLatestEntryIdForCategory(AtomCategory category) {
		return atomEntryDAO.getLatestEntryIdForCategory(category).orElse(null);
	}

	private List<AtomEntry> getEntriesForFeed(AtomFeed atomFeed) {
		List<AtomEntry> atomEntries = atomEntryDAO.getAtomEntriesForFeed(atomFeed.getId());
		for (AtomEntry atomEntry : atomEntries) {
			atomEntry.setAtomCategories(atomCategoryDAO.getCategoriesForAtomEntry(atomEntry.getAtomEntryId()));
			atomEntry.setAtomLink(atomLinkDAO.findBy(atomEntry.getAtomEntryId()));
			atomEntry.setAuthors(atomAuthorDAO.findBy(atomEntry.getAtomEntryId()));
		}
		return atomEntries;
	}
}
