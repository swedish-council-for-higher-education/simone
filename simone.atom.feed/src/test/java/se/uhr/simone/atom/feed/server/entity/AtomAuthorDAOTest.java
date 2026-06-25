package se.uhr.simone.atom.feed.server.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;

@ExtendWith(DataSourceParameterResolver.class)
public class AtomAuthorDAOTest {

	private static final Person AUTHOR = Person.of("AUTHOR TEST");

	private AtomAuthorDAO atomAuthorDAO;

	private String atomEntryId = createNewAtomEntryId();

	@BeforeEach
	public void setup(DataSource ds) {
		atomAuthorDAO = new AtomAuthorDAO(new JdbcTemplate(ds));
		AtomEntryDAO atomEntryDAO = new AtomEntryDAO(new JdbcTemplate(ds));
		atomEntryDAO.insert(AtomEntry.builder().withAtomEntryId(atomEntryId).withSortOrder(1L).withSubmittedNow().build());
	}

	@Test
	public void notExists() {
		assertThat(atomAuthorDAO.exists(atomEntryId)).isFalse();
	}

	@Test
	public void exists() {
		atomAuthorDAO.insert(atomEntryId, AUTHOR);
		assertThat(atomAuthorDAO.exists(atomEntryId)).isTrue();
	}

	@Test
	public void insert() {
		atomAuthorDAO.insert(atomEntryId, AUTHOR);
	}

	@Test
	public void emptyListWhenNoResult() {
		assertThat(atomAuthorDAO.findBy(UUID.randomUUID().toString())).isEmpty();
	}

	@Test
	public void findsAllAuthorsForEntryId() {

		atomAuthorDAO.insert(atomEntryId, AUTHOR);
		atomAuthorDAO.insert(atomEntryId, AUTHOR);
		assertThat(atomAuthorDAO.findBy(atomEntryId)).hasSize(2);
	}

	@Test
	public void deleteAllAuthorsForEntry() {
		atomAuthorDAO.insert(atomEntryId, AUTHOR);
		atomAuthorDAO.delete(atomEntryId);
		assertThat(atomAuthorDAO.findBy(atomEntryId)).isEmpty();
	}

	private String createNewAtomEntryId() {
		return UUID.randomUUID().toString();
	}
}
