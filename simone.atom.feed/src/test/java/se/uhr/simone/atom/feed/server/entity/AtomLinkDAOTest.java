package se.uhr.simone.atom.feed.server.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import javax.sql.DataSource;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;

@ExtendWith(DataSourceParameterResolver.class)
public class AtomLinkDAOTest {

	private AtomLinkDAO atomLinkDAO;

	private String id = createAtomEntryId();

	@BeforeEach
	public void setup(DataSource ds) {
		atomLinkDAO = new AtomLinkDAO(new JdbcTemplate(ds));
		AtomEntryDAO atomEntryDAO = new AtomEntryDAO(new JdbcTemplate(ds));
		atomEntryDAO.insert(AtomEntry.builder().withAtomEntryId(id).withSortOrder(1L).withSubmittedNow().build());
	}

	@Test
	public void notExists() {
		assertThat(atomLinkDAO.exists(id)).isFalse();
	}

	@Test
	public void exists() {
		atomLinkDAO.insert(id, createAtomLink());

		assertThat(atomLinkDAO.exists(id)).isTrue();
	}

	@Test
	public void insert() {
		atomLinkDAO.insert(id, createAtomLink());
	}

	@Test
	public void emptyListWhenNoResult() {
		assertThat(atomLinkDAO.findBy(UUID.randomUUID().toString())).isEmpty();
	}

	@Test
	public void findsAllLinksForEntryId() {
		atomLinkDAO.insert(id, createAtomLink());
		assertThat(atomLinkDAO.findBy(id)).isNotEmpty();
	}

	@Test
	public void deleteAllLinksForEntry() throws Exception {
		atomLinkDAO.insert(id, createAtomLink());
		atomLinkDAO.delete(id);
		assertThat(atomLinkDAO.findBy(id)).isEmpty();
	}

	private String createAtomEntryId() {
		return UUID.randomUUID().toString();
	}

	private AtomLink createAtomLink() {
		return AtomLink.builder()
				.withRelAlternate()
				.withHref("http://exaple.com/api/resource/1")
				.withType(MediaType.APPLICATION_JSON)
				.build();
	}
}
