package se.uhr.simone.core.entity;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.uhr.simone.atom.feed.utils.jdbc.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class SqlScriptRunnerTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@InjectMocks
	private SqlScriptRunner runner;

	@Test
	void testExecute() {

		runner.execute(this.getClass().getResourceAsStream("/test.sql"));

		ArgumentCaptor<String> stmtCaptor = ArgumentCaptor.forClass(String.class);

		verify(jdbcTemplate, times(8)).execute(stmtCaptor.capture());
	}
}
