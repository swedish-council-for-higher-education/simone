package se.uhr.simone.core;

import java.net.URI;
import se.uhr.simone.core.feed.control.AtomEntry;
import se.uhr.simone.atom.feed.server.control.FeedConverter;
import se.uhr.simone.atom.feed.server.control.FeedCreator;
import se.uhr.simone.atom.feed.server.control.FeedXmlCreator;
import se.uhr.simone.core.feed.control.FeedPublisher;
import se.uhr.simone.core.feed.entity.SimoneFeedRepository;

public class SimOne {

	private final String name;
	private final URI feedBaseURI;
	private final Runnable clearDatabaseFunction;

	private final FeedCreator feedCreator;
	private final FeedXmlCreator feedXmlCreator;
	private final SimoneFeedRepository feedRepository;
	private final FeedPublisher feedPublisher;

	private final FeedConverter feedConverter;

	public SimOne(String name, URI feedBaseURI, SimoneFeedRepository feedRepository, Runnable clearDatabaseFunction) {
		this.name = name;
		this.feedBaseURI = feedBaseURI;
		this.feedRepository = feedRepository;
		this.clearDatabaseFunction = clearDatabaseFunction;

		feedConverter = new FeedConverter();
		feedCreator = new FeedCreator();
		feedXmlCreator = new FeedXmlCreator(feedConverter);
		feedPublisher = new FeedPublisher(feedRepository);
	}

	public String getName() {
		return name;
	}

	public URI getFeedBaseURI() {
		return feedBaseURI;
	}

	public void connectEntrysToFeeds() {
		feedCreator.connectEntrysToFeeds(feedRepository);
	}

	public void createXmlForFeeds() {
		feedXmlCreator.createXmlForFeeds(feedRepository, feedBaseURI);
	}

	public void publish(AtomEntry atomEntry) {
		feedPublisher.publish(atomEntry);
	}

	public FeedConverter getFeedConverter() {
		return feedConverter;
	}

	public SimoneFeedRepository getFeedRepository() {
		return feedRepository;
	}

	public FeedPublisher getFeedPublisher() {
		return feedPublisher;
	}

	public void clearDatabase() {
		feedRepository.clear();
		clearDatabaseFunction.run();
	}

	public static NameStep builder() {
		return new SimOneBuilder();
	}

	public interface NameStep {
		FeedBaseURIStep withName(String name);
	}

	public interface FeedBaseURIStep {
		SimOneBuilder withFeedBaseURI(URI feedBaseURI);
	}


	public static class SimOneBuilder implements NameStep, FeedBaseURIStep {

		private String name;

		private URI feedBaseURI;
		private SimoneFeedRepository feedRepository;
		private Runnable clearDatabaseFunction = () -> {
		};

		@Override
		public FeedBaseURIStep withName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public SimOneBuilder withFeedBaseURI(URI feedBaseURI) {
			this.feedBaseURI = feedBaseURI;
			return this;
		}

		public SimOneBuilder withFeedRepository(SimoneFeedRepository feedRepository) {
			this.feedRepository = feedRepository;
			return this;
		}

		public SimOneBuilder withClearDatabaseFunction(Runnable clearDatabaseFunction) {
			this.clearDatabaseFunction = clearDatabaseFunction;
			return this;
		}

		public SimOne build() {
			return new SimOne(name, feedBaseURI, feedRepository, clearDatabaseFunction);
		}
	}
}
