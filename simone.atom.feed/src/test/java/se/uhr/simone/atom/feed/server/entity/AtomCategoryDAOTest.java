package se.uhr.simone.atom.feed.server.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import se.uhr.simone.atom.feed.server.entity.AtomCategory.Label;
import se.uhr.simone.atom.feed.server.entity.AtomCategory.Term;
import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;
import se.uhr.simone.atom.feed.utils.jdbc.PersistenceException;

@ExtendWith(DataSourceParameterResolver.class)
public class AtomCategoryDAOTest {

	private AtomCategoryDAO atomCategoryDAO;

	private AtomEntryDAO atomEntryDAO;

	@BeforeEach
	public void setup(DataSource ds) {
		atomCategoryDAO = new AtomCategoryDAO(new JdbcTemplate(ds));
		atomEntryDAO = new AtomEntryDAO(new JdbcTemplate(ds));
	}

	@Test
	public void isConnectedShouldReturnFalse() {
		assertThat(atomCategoryDAO.isConnected(createAtomCategory(), UUID.randomUUID().toString())).isFalse();
	}

	@Test
	public void isConnectedShouldReturnTrue() {

		AtomEntry atomEntry = createAtomEntry();
		atomEntryDAO.insert(atomEntry);

		AtomCategory atomCategory = createAtomCategory();
		atomCategoryDAO.connectEntryToCategory(atomEntry.getAtomEntryId(), atomCategory);

		assertThat(atomCategoryDAO.isConnected(atomCategory, atomEntry.getAtomEntryId())).isTrue();
	}

	@Test
	public void connectEntryToCategoryShouldThrowExceptionWhenEntryDoesNotExist() {
		assertThatExceptionOfType(PersistenceException.class).isThrownBy(() -> {
			atomCategoryDAO.connectEntryToCategory(UUID.randomUUID().toString(),
					AtomCategory.builder().withTerm(Term.of("term")).withLabel(Label.of("label")).build());
		});
	}

	@Test
	public void getCategoriesForAtomEntryShouldReturnEmptyList() {
		assertThat(atomCategoryDAO.getCategoriesForAtomEntry(UUID.randomUUID().toString())).isEmpty();
	}

	@Test
	public void getCategoriesForAtomEntry() {
		AtomEntry atomEntry = createAtomEntry();
		atomEntryDAO.insert(atomEntry);

		atomCategoryDAO.connectEntryToCategory(atomEntry.getAtomEntryId(),
				AtomCategory.builder().withTerm(Term.of("term1")).withLabel(Label.of("label1")).build());
		atomCategoryDAO.connectEntryToCategory(atomEntry.getAtomEntryId(),
				AtomCategory.builder().withTerm(Term.of("term2")).withLabel(Label.of("label2")).build());
		atomCategoryDAO.connectEntryToCategory(atomEntry.getAtomEntryId(),
				AtomCategory.builder().withTerm(Term.of("term3")).withLabel(Label.of("label3")).build());

		assertThat(atomCategoryDAO.getCategoriesForAtomEntry(atomEntry.getAtomEntryId())).hasSize(3);
	}

	private AtomEntry createAtomEntry() {
		return AtomEntry.builder()
				.withAtomEntryId(UUID.randomUUID().toString())
				.withSortOrder(Long.valueOf(1))
				.withSubmittedNow()
				.build();
	}

	private AtomCategory createAtomCategory() {
		return AtomCategory.builder().withTerm(Term.of("term")).withLabel(Label.of("label")).build();
	}
}
