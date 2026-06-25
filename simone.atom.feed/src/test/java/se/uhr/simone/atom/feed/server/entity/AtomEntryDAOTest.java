package se.uhr.simone.atom.feed.server.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;
import se.uhr.simone.atom.feed.utils.jdbc.PersistenceException;

@ExtendWith(DataSourceParameterResolver.class)
public class AtomEntryDAOTest {

	private static final long FIRST_NON_EXISTING_FEED_ID = 2L;

	private AtomFeedDAO atomFeedDAO;
	private AtomEntryDAO atomEntryDAO;

	@BeforeEach
	public void setup(DataSource ds) {
		atomFeedDAO = new AtomFeedDAO(new JdbcTemplate(ds));
		atomEntryDAO = new AtomEntryDAO(new JdbcTemplate(ds));
	}

	@Test
	public void notExists() {
		assertThat(atomEntryDAO.exists(createAtomEntryId())).isFalse();
	}

	@Test
	public void exists() {
		AtomEntry atomEntry = createAtomEntry();
		atomEntryDAO.insert(atomEntry);

		assertThat(atomEntryDAO.exists(atomEntry.getAtomEntryId())).isTrue();
	}

	@Test
	public void insert() {
		atomEntryDAO.insert(createAtomEntry());
	}

	@Test
	public void insertTooBigContentShouldThrowException() {
		String xml = "<xml>2</xml>".repeat(1_000); // 12_000 characters, limit 11_400
		AtomEntry atomEntry = createAtomEntry();
		atomEntry.setContent(Content.builder().withValue(xml).withContentType(MediaType.APPLICATION_XML).build());

		assertThatExceptionOfType(PersistenceException.class).isThrownBy(() -> {
			atomEntryDAO.insert(atomEntry);
		});
	}

	@Test
	public void updateNonExisting() {
		atomEntryDAO.update(createAtomEntry());
	}

	@Test
	public void update() {
		AtomEntry atomEntry = createAtomEntry();

		atomEntryDAO.insert(atomEntry);

		atomEntry.setContent(
				Content.builder().withValue("<xml><value>2</value></xml>").withContentType(MediaType.APPLICATION_XML).build());

		atomEntryDAO.update(atomEntry);

		var fetchedAtomEntry = atomEntryDAO.fetchBy(atomEntry.getAtomEntryId());

		assertThat(fetchedAtomEntry).hasValueSatisfying(entry -> {
			assertThat(entry.getContent().get().getValue()).isEqualTo(atomEntry.getContent().get().getValue());
		});
	}

	@Test
	public void fetchByShouldThrowExceptionWhenNotExisting() {
		assertThat(atomEntryDAO.fetchBy(UUID.randomUUID().toString())).isEmpty();
	}

	@Test
	public void getAtomEntriesForFeedShouldReturnEmptyList() {
		assertThat(atomEntryDAO.getAtomEntriesForFeed(1)).isEmpty();
	}

	@Test
	public void getAtomEntriesForFeedShouldReturnOrderedList() {
		atomFeedDAO.insert(new AtomFeed(FIRST_NON_EXISTING_FEED_ID));

		String id1 = UUID.randomUUID().toString();
		String id2 = UUID.randomUUID().toString();
		String id3 = UUID.randomUUID().toString();
		atomEntryDAO.insert(AtomEntry.builder()
				.withAtomEntryId(id1)
				.withSortOrder(Long.valueOf(2))
				.withSubmittedNow()
				.withFeedId(Long.valueOf(1))
				.build());

		atomEntryDAO.insert(AtomEntry.builder()
				.withAtomEntryId(id2)
				.withSortOrder(Long.valueOf(3))
				.withSubmittedNow()
				.withFeedId(Long.valueOf(1))
				.build());

		atomEntryDAO.insert(AtomEntry.builder()
				.withAtomEntryId(id3)
				.withSortOrder(Long.valueOf(1))
				.withSubmittedNow()
				.withFeedId(Long.valueOf(1))
				.build());

		List<AtomEntry> entriesForFeed = atomEntryDAO.getAtomEntriesForFeed(1);
		assertThat(entriesForFeed.get(0).getAtomEntryId()).isEqualTo(id2);
		assertThat(entriesForFeed.get(1).getAtomEntryId()).isEqualTo(id1);
		assertThat(entriesForFeed.get(2).getAtomEntryId()).isEqualTo(id3);
	}

	@Test
	public void getAtomEntriesForFeedShouldReturnOrderedListWithSameSubmitTime() {
		atomFeedDAO.insert(new AtomFeed(FIRST_NON_EXISTING_FEED_ID));

		Timestamp now = new Timestamp(System.currentTimeMillis());

		String id1 = UUID.randomUUID().toString();
		String id2 = UUID.randomUUID().toString();
		String id3 = UUID.randomUUID().toString();
		atomEntryDAO.insert(AtomEntry.builder()
				.withAtomEntryId(id1)
				.withSortOrder(Long.valueOf(1))
				.withSubmitted(now)
				.withFeedId(Long.valueOf(1))
				.build());

		atomEntryDAO.insert(AtomEntry.builder()
				.withAtomEntryId(id2)
				.withSortOrder(Long.valueOf(2))
				.withSubmitted(now)
				.withFeedId(Long.valueOf(1))
				.build());

		atomEntryDAO.insert(AtomEntry.builder()
				.withAtomEntryId(id3)
				.withSortOrder(Long.valueOf(3))
				.withSubmitted(now)
				.withFeedId(Long.valueOf(1))
				.build());

		for (int i = 1; i <= 2; i++) {
			List<AtomEntry> entriesForFeed = atomEntryDAO.getAtomEntriesForFeed(1);
			assertThat(entriesForFeed.get(0).getAtomEntryId()).isEqualTo(id3);
			assertThat(entriesForFeed.get(1).getAtomEntryId()).isEqualTo(id2);
			assertThat(entriesForFeed.get(2).getAtomEntryId()).isEqualTo(id1);
		}

	}

	@Test
	public void shouldFetchCorrectTimestamp() {
		atomFeedDAO.insert(new AtomFeed(FIRST_NON_EXISTING_FEED_ID));

		Timestamp ts = Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"));

		String id1 = UUID.randomUUID().toString();
		atomEntryDAO.insert(AtomEntry.builder()
				.withAtomEntryId(id1)
				.withSortOrder(1L)
				.withSubmitted(ts)
				.withFeedId(1L)
				.build());

		List<AtomEntry> entriesForFeed = atomEntryDAO.getAtomEntriesForFeed(1);

		assertThat(entriesForFeed).singleElement().satisfies(e -> {
			assertThat(e.getSubmitted()).isEqualTo(ts);
		});
	}

	@Test
	public void getEntriesNotConnectedToFeedShouldReturnEmptyList() {
		assertThat(atomEntryDAO.getEntriesNotConnectedToFeed()).isEmpty();
	}

	@Test
	public void getEntriesNotConnectedToFeedShouldReturnOrderedList() {

		String id1 = UUID.randomUUID().toString();
		String id2 = UUID.randomUUID().toString();
		String id3 = UUID.randomUUID().toString();
		atomEntryDAO.insert(AtomEntry.builder().withAtomEntryId(id1).withSortOrder(Long.valueOf(2)).withSubmittedNow().build());

		atomEntryDAO.insert(AtomEntry.builder().withAtomEntryId(id2).withSortOrder(Long.valueOf(3)).withSubmittedNow().build());

		atomEntryDAO.insert(AtomEntry.builder().withAtomEntryId(id3).withSortOrder(Long.valueOf(1)).withSubmittedNow().build());

		List<AtomEntry> entriesNotConnectedToFeed = atomEntryDAO.getEntriesNotConnectedToFeed();
		assertThat(entriesNotConnectedToFeed.get(0).getAtomEntryId()).isEqualTo(id3);
		assertThat(entriesNotConnectedToFeed.get(1).getAtomEntryId()).isEqualTo(id1);
		assertThat(entriesNotConnectedToFeed.get(2).getAtomEntryId()).isEqualTo(id2);
	}

	@Test
	public void getEntriesNotConnectedToFeedShouldReturnMaxNumberOfItems() {
		for (int i = 0; i < AtomEntryDAO.MAX_NUM_OF_ENTRIES_TO_RETURN + 1; i++) {
			String id = UUID.randomUUID().toString();
			atomEntryDAO.insert(AtomEntry.builder().withAtomEntryId(id).withSortOrder(Long.valueOf(1)).withSubmittedNow().build());
		}

		List<AtomEntry> entriesNotConnectedToFeed = atomEntryDAO.getEntriesNotConnectedToFeed();
		assertThat(entriesNotConnectedToFeed).hasSize(AtomEntryDAO.MAX_NUM_OF_ENTRIES_TO_RETURN);
	}

	private String createAtomEntryId() {
		return UUID.randomUUID().toString();
	}

	private AtomEntry createAtomEntry() {
		return AtomEntry.builder()
				.withAtomEntryId(createAtomEntryId())
				.withSortOrder(Long.valueOf(1))
				.withSubmittedNow()
				.withContent(
						Content.builder().withValue("<xml><value>1</value></xml>").withContentType(MediaType.APPLICATION_XML).build())
				.build();
	}

}
