package se.uhr.simone.atom.feed.server.entity;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;

@ExtendWith(DataSourceParameterResolver.class)
public class AtomFeedDAOTest {

    private static final String TEST_XML_CONTENT = "<xml><value>1</value></xml>";

    private static final long FIRST_NON_EXISTING_FEED_ID = 2L;

    private AtomFeedDAO atomFeedDAO;

    @BeforeEach
    public void setup(DataSource ds) {
        atomFeedDAO = new AtomFeedDAO(new JdbcTemplate(ds));
    }

    @Test
    public void exists() {
        AtomFeed atomFeed = new AtomFeed(FIRST_NON_EXISTING_FEED_ID);
        atomFeedDAO.insert(atomFeed);

        assertThat(atomFeedDAO.exists(FIRST_NON_EXISTING_FEED_ID)).isTrue();
    }

    @Test
    public void notExists() {
        assertThat(atomFeedDAO.exists(FIRST_NON_EXISTING_FEED_ID)).isFalse();
    }

    @Test
    public void insert() {
        atomFeedDAO.insert(createAtomFeed());
    }

    @Test
    public void insertShouldHaveNullValues() {
        atomFeedDAO.insert(new AtomFeed(FIRST_NON_EXISTING_FEED_ID));

        var fetchedAtomFeed = atomFeedDAO.fetchBy(FIRST_NON_EXISTING_FEED_ID);

        assertThat(fetchedAtomFeed).hasValueSatisfying(f -> {
            assertThat(f.getNextFeedId()).isNull();
            assertThat(f.getPreviousFeedId()).isNull();
            assertThat(f.getXml()).isNull();
        });
    }

    @Test
    public void updateNotExisting() {
        assertThat(atomFeedDAO.update(createAtomFeed())).isEqualTo(0);
    }

    @Test
    public void update() {

        AtomFeed atomFeed = createAtomFeed();
        atomFeedDAO.insert(atomFeed);

        atomFeed.setNextFeedId(Long.valueOf(24));
        atomFeed.setPreviousFeedId(Long.valueOf(23));
        atomFeed.setXml("<xml><value>2</value></xml>");

        atomFeedDAO.update(atomFeed);

        var fetchedAtomFeed = atomFeedDAO.fetchBy(FIRST_NON_EXISTING_FEED_ID);

        assertThat(fetchedAtomFeed).hasValueSatisfying(f -> {
            assertThat(f.getId()).isEqualTo(atomFeed.getId());
            assertThat(f.getNextFeedId()).isEqualTo(atomFeed.getNextFeedId());
            assertThat(f.getPreviousFeedId()).isEqualTo(atomFeed.getPreviousFeedId());
            assertThat(f.getXml()).isEqualTo(atomFeed.getXml());
        });

    }

    @Test
    public void fetchByNotExisting() {
        assertThat(atomFeedDAO.fetchBy(FIRST_NON_EXISTING_FEED_ID)).isEmpty();
    }

    @Test
    public void fetchBy() {
        AtomFeed atomFeed = createAtomFeed();
        atomFeedDAO.insert(atomFeed);

        var fetchedAtomFeed = atomFeedDAO.fetchBy(atomFeed.getId());

        assertThat(fetchedAtomFeed).hasValueSatisfying(f -> {
            assertThat(f.getId()).isEqualTo(atomFeed.getId());
            assertThat(f.getXml()).isEqualTo(atomFeed.getXml());
        });
    }

    @Test
    public void fetchRecentNothingInserted() {
        var feed = atomFeedDAO.fetchRecent();

        assertThat(feed).hasValueSatisfying(f -> {
            assertThat(f.getId()).isEqualTo(1L);
        });
    }

    @Test
    public void fetchRecent() {
        atomFeedDAO.insert(createAtomFeed());

        var recent = atomFeedDAO.fetchRecent();

        assertThat(recent).hasValueSatisfying(f -> {
            assertThat(f.getId()).isEqualTo(FIRST_NON_EXISTING_FEED_ID);
        });
    }

    @Test
    public void getFeedsWithoutXmlNoFeedsExisting() {
        assertThat(atomFeedDAO.getFeedsWithoutXml().size()).isEqualTo(0);
    }

    @Test
    public void getFeedsWithoutXmlOnlyRecentExists() {
        AtomFeed atomFeed = new AtomFeed(2);
        atomFeedDAO.insert(atomFeed);

        assertThat(atomFeedDAO.getFeedsWithoutXml().size()).isEqualTo(0);
    }

    @Test
    public void getFeedsWithoutXmlAllFeedsHaveXml() {
        atomFeedDAO.insert(createAtomFeed());
        assertThat(atomFeedDAO.getFeedsWithoutXml()).hasSize(0);
    }

    @Test
    public void getFeedsWithoutXml() {
        AtomFeed atomFeed = createAtomFeed();
        atomFeed.setXml(null);
        atomFeedDAO.insert(atomFeed);

        assertThat(atomFeedDAO.getFeedsWithoutXml()).hasSize(1);
    }

    @Test
    public void testSaveAtomFeedXml() {
        AtomFeed atomFeed = createAtomFeedWithoutXml();
        atomFeedDAO.insert(atomFeed);
        assertThat(atomFeed.getXml()).isNull();

        assertThat(atomFeedDAO.saveAtomFeedXml(atomFeed.getId(), TEST_XML_CONTENT)).isEqualTo(1);

        var atomFeedFromDatabase = atomFeedDAO.fetchBy(atomFeed.getId());

        assertThat(atomFeedFromDatabase).hasValueSatisfying(f -> {
            assertThat(f.getXml()).isEqualTo(TEST_XML_CONTENT);
        });
    }

    private AtomFeed createAtomFeed() {
        AtomFeed res = createAtomFeedWithoutXml();
        res.setXml(TEST_XML_CONTENT);
        return res;
    }

    private AtomFeed createAtomFeedWithoutXml() {
        AtomFeed atomFeed = new AtomFeed(FIRST_NON_EXISTING_FEED_ID);
        atomFeed.setNextFeedId((long) 3);
        atomFeed.setPreviousFeedId((long) 1);
        return atomFeed;
    }
}
