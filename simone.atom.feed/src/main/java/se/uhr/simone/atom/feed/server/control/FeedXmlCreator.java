package se.uhr.simone.atom.feed.server.control;

import java.net.URI;
import java.util.List;

import se.uhr.simone.atom.feed.server.entity.AtomFeed;
import se.uhr.simone.atom.feed.server.entity.AbstractFeedRepository;

public class FeedXmlCreator {

	private final FeedConverter feedConverter;

	public FeedXmlCreator(FeedConverter feedConverter) {
		this.feedConverter = feedConverter;
	}

	/**
	 * Fetches all {@link AtomFeed}s that are "full" and converts them to xml if not already converted.
	 * 
	 * @param feedRepository The {@link AbstractFeedRepository} to fetch {@link AtomFeed}s from.
	 * @param baseUri The {@link URI} used for building links to next and previous feed.
     *
     * @apiNote
     * This method must run in a transaction.
	 */

	public void createXmlForFeeds(AbstractFeedRepository feedRepository, URI baseUri) {

		List<AtomFeed> feedsWithoutXml = feedRepository.getFeedsWithoutXml();

		for (AtomFeed feed : feedsWithoutXml) {
			feedRepository.saveAtomFeedXml(feed.getId(), feedConverter.convertFeedToXml(feed, baseUri));
		}
	}
}
