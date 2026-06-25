package se.uhr.simone.atom.feed.server.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedRepositoryTest {

	private final AtomFeedDAO atomFeedDAO = mock(AtomFeedDAO.class);

	private final AtomEntryDAO atomEntryDAO = mock(AtomEntryDAO.class);

	private final AtomCategoryDAO atomCategoryDAO = mock(AtomCategoryDAO.class);

	private final AtomLinkDAO atomLinkDAO = mock(AtomLinkDAO.class);

	private final AtomAuthorDAO atomAuthorDAO = mock(AtomAuthorDAO.class);

	private final AbstractFeedRepository
			feedRepository = new AbstractFeedRepository(atomFeedDAO, atomEntryDAO, atomCategoryDAO, atomLinkDAO, atomAuthorDAO) {

		@Override
		public void clear() {}
	};

	@Test
	void saveAtomFeedWithoutEntries() {
		AtomFeed atomFeed = new AtomFeed(1);

		given(atomFeedDAO.exists(1)).willReturn(true);

		feedRepository.saveAtomFeed(atomFeed);

		verify(atomFeedDAO, times(1)).exists(1);
		verify(atomFeedDAO, times(1)).update(atomFeed);
		verify(atomEntryDAO, never()).exists(any(String.class));
	}

	@Test
	void saveAtomFeedWithEntries() {
		AtomFeed atomFeed = new AtomFeed(1);
		AtomEntry atomEntry = createAtomEntry();

		atomFeed.getEntries().add(atomEntry);

		given(atomFeedDAO.exists(1)).willReturn(false);
		given(atomEntryDAO.exists(atomEntry.getAtomEntryId())).willReturn(false);

		feedRepository.saveAtomFeed(atomFeed);

		verify(atomFeedDAO, times(1)).exists(1);
		verify(atomFeedDAO, times(1)).insert(atomFeed);
		verify(atomEntryDAO, times(1)).exists(atomEntry.getAtomEntryId());
		verify(atomEntryDAO, times(1)).insert(atomEntry);
	}

	@Test
	void saveAtomEntryWithoutCategories() {
		AtomEntry atomEntry = createAtomEntry();

		given(atomEntryDAO.exists(atomEntry.getAtomEntryId())).willReturn(true);

		feedRepository.saveAtomEntry(atomEntry);

		verify(atomEntryDAO, times(1)).exists(atomEntry.getAtomEntryId());
		verify(atomEntryDAO, times(1)).update(atomEntry);
		verify(atomLinkDAO, times(1)).delete(atomEntry.getAtomEntryId());
		verify(atomAuthorDAO, times(1)).delete(atomEntry.getAtomEntryId());
		//		verify(atomCategoryDAO, never()).exists(any(Long.class));
		verify(atomCategoryDAO, never()).isConnected(any(AtomCategory.class), eq(atomEntry.getAtomEntryId()));
	}

	@Test
	void getFeedByIdNotExisting() {
		given(atomFeedDAO.fetchBy(1)).willReturn(Optional.empty());

		AtomFeed feed = feedRepository.getFeedById(1);
		assertThat(feed).isNull();

		verify(atomFeedDAO, times(1)).fetchBy(1);
		verify(atomEntryDAO, never()).getAtomEntriesForFeed(1);
	}

	@Test
	void getFeedById() {
		AtomFeed atomFeed = new AtomFeed(1);

		given(atomFeedDAO.fetchBy(1)).willReturn(Optional.of(atomFeed));
		given(atomEntryDAO.getAtomEntriesForFeed(1)).willReturn(List.of(createAtomEntry()));

		AtomFeed fetchedFeed = feedRepository.getFeedById(1);

		assertThat(fetchedFeed).isNotNull();
		assertThat(fetchedFeed.getEntries()).hasSize(1);

		verify(atomFeedDAO, times(1)).fetchBy(1);
		verify(atomEntryDAO, times(1)).getAtomEntriesForFeed(1);
	}

	@Test
	void getRecentFeedMustExistInDatabase() {
		given(atomFeedDAO.fetchRecent()).willReturn(Optional.empty());
		assertThat(feedRepository.getRecentFeed()).isNull();
	}

	@Test
	void getRecentFeed() {
		AtomFeed atomFeed = new AtomFeed(1);

		given(atomFeedDAO.fetchRecent()).willReturn(Optional.of(atomFeed));
		given(atomEntryDAO.getAtomEntriesForFeed(1)).willReturn(Arrays.asList(createAtomEntry()));

		AtomFeed fetchedFeed = feedRepository.getRecentFeed();

		assertThat(fetchedFeed).isNotNull();
		assertThat(fetchedFeed.getEntries()).hasSize(1);

		verify(atomFeedDAO, times(1)).fetchRecent();
		verify(atomEntryDAO, times(1)).getAtomEntriesForFeed(1);
	}

	private AtomEntry createAtomEntry() {
		return AtomEntry.builder()
				.withAtomEntryId(UUID.randomUUID().toString())
				.withSortOrder(Long.valueOf(1))
				.withSubmittedNow()
				.withContent(Content.builder().withValue("<xml></xml>").withContentType(MediaType.APPLICATION_XML).build())
				.build();
	}
}
