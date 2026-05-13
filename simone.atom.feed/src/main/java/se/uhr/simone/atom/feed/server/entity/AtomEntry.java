package se.uhr.simone.atom.feed.server.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AtomEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String atomEntryId;
	private Content content;

	private Long feedId;
	private Long sortOrder;
	private Timestamp submitted;
	private String title;
	private List<AtomCategory> atomCategories = new ArrayList<>();
	private List<AtomLink> atomLinks = new ArrayList<>();
	private List<Person> authors = new ArrayList<>();
	private Content summary;

	private AtomEntry(AtomEntryBuilder builder) {
		this.atomEntryId = builder.atomEntryId;
		this.content = builder.content;
		this.submitted = builder.submitted;
		this.atomCategories = builder.categories;
		this.feedId = builder.feedId;
		this.sortOrder = builder.sortOrder;
		this.title = builder.title;
		this.atomLinks = builder.links;
		this.authors = builder.authors;
		this.summary = builder.summary;
	}

	public static AtomEntryIdBuilder builder() {
		return new AtomEntryBuilder();
	}

	public Long getSortOrder() {
		return sortOrder;
	}

	public String getAtomEntryId() {
		return atomEntryId;
	}

	public Optional<Content> getContent() {
		return Optional.ofNullable(content);
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public Long getFeedId() {
		return feedId;
	}

	public void setFeedId(Long feedId) {
		this.feedId = feedId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public Timestamp getSubmitted() {
		return submitted;
	}

	public List<AtomCategory> getAtomCategories() {
		return atomCategories;
	}

	public void setAtomCategories(List<AtomCategory> atomCategories) {
		this.atomCategories = atomCategories;
	}

	public boolean hasTitle() {
		return Objects.nonNull(title) && !title.trim().isEmpty();
	}

	public List<AtomLink> getAtomLinks() {
		return atomLinks;
	}

	public void setAtomLink(List<AtomLink> atomLink) {
		this.atomLinks = atomLink;
	}

	public List<Person> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Person> authors) {
		this.authors = authors;
	}

	public Optional<Content> getSummary() {
		return Optional.ofNullable(summary);
	}

	public void setSummary(Content summary) {
		this.summary = summary;
	}

	public static class AtomEntryBuilder implements AtomEntryIdBuilder, AtomEntrySortOrderBuilder, SubmittedBuilder, Build {

		private Long sortOrder;
		private Timestamp submitted;
		private String atomEntryId;
		private Content content;
		private List<AtomCategory> categories = new ArrayList<>();
		private Long feedId;
		private String title;
		private List<AtomLink> links = new ArrayList<>();
		private List<Person> authors = new ArrayList<>();
		private Content summary;

		@Override
		public AtomEntry build() {
			return new AtomEntry(this);
		}

		@Override
		public Build withSubmittedNow() {
			this.submitted = new Timestamp(System.currentTimeMillis());
			return this;
		}

		@Override
		public Build withSubmitted(Timestamp submitted) {
			this.submitted = submitted;
			return this;
		}

		@Override
		public AtomEntrySortOrderBuilder withAtomEntryId(String atomEntryId) {
			this.atomEntryId = atomEntryId;
			return this;
		}

		@Override
		public Build withContent(Content content) {
			this.content = content;
			return this;
		}

		@Override
		public Build withCategory(AtomCategory atomCategory) {
			this.categories.add(atomCategory);
			return this;
		}

		@Override
		public Build withFeedId(Long id) {
			this.feedId = id;
			return this;
		}

		@Override
		public Build withCategories(List<AtomCategory> atomCategories) {
			this.categories = atomCategories;
			return this;
		}

		@Override
		public SubmittedBuilder withSortOrder(Long order) {
			this.sortOrder = order;
			return this;
		}

		@Override
		public Build withTitle(String title) {
			this.title = title;
			return this;
		}

		@Override
		public Build withLinks(List<AtomLink> links) {
			this.links = links;
			return this;
		}

		@Override
		public Build withAuthor(List<Person> authors) {
			this.authors = authors;
			return this;
		}

		@Override
		public Build withSummary(Content summary) {
			this.summary = summary;
			return this;
		}

	}

	public interface AtomEntryIdBuilder {

		public AtomEntrySortOrderBuilder withAtomEntryId(String atomEntryId);
	}

	public interface AtomEntrySortOrderBuilder {

		public SubmittedBuilder withSortOrder(Long order);
	}

	public interface SubmittedBuilder {

		public Build withSubmittedNow();

		public Build withSubmitted(Timestamp submitted);
	}

	public interface Build {

		public Build withFeedId(Long id);

		public Build withContent(Content content);

		public Build withCategory(AtomCategory atomCategory);

		public Build withCategories(List<AtomCategory> atomCategories);

		public Build withTitle(String title);

		public Build withLinks(List<AtomLink> links);

		public Build withAuthor(List<Person> authors);

		public Build withSummary(Content summary);

		public AtomEntry build();

	}

}