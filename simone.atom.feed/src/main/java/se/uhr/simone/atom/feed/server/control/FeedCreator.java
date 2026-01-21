package se.uhr.simone.atom.feed.server.control;

import java.util.Iterator;
import java.util.List;

import se.uhr.simone.atom.feed.server.entity.AtomEntry;
import se.uhr.simone.atom.feed.server.entity.AtomFeed;
import se.uhr.simone.atom.feed.server.entity.AbstractFeedRepository;

public class FeedCreator {

	/**
	 * Connects {@link AtomEntry}s that are not connected to a {@link AtomFeed}.
	 * 
	 * @param feedRepository The {@link AbstractFeedRepository} to fetch {@link AtomEntry}s and {@link AtomFeed}s from.
     *
     * @apiNote
     * This method must run in a transaction.
	 */

	public void connectEntrysToFeeds(AbstractFeedRepository feedRepository) {
		List<AtomEntry> entriesWithoutFeed = feedRepository.getEntriesNotConnectedToFeed();
		if (entriesWithoutFeed.isEmpty()) {
			return;
		}

		AtomFeed currentFeed = feedRepository.getRecentFeed();

		Iterator<AtomEntry> entries = entriesWithoutFeed.iterator();

		while (entries.hasNext()) {
			AtomEntry entry = entries.next();

			if (!currentFeed.addEntry(entry)) {
				AtomFeed nextFeed = currentFeed.createNextAtomFeed();
				nextFeed.addEntry(entry);
				feedRepository.saveAtomFeed(nextFeed);
				feedRepository.saveAtomFeed(currentFeed);
				currentFeed = nextFeed;
			}
		}

		feedRepository.saveAtomFeed(currentFeed);
	}

}