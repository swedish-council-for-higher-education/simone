package se.uhr.simone.atom.feed.utils.jdbc;


import se.uhr.simone.atom.feed.utils.jdbc.internal.SqlParams;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

	private final DataSource dataSource;

	public JdbcTemplate(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object param) {
		return queryForObject(sql, rowMapper, Arrays.asList(param));
	}

	public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
		return queryForObject(sql, rowMapper, Arrays.asList(params));
	}

	public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, List<Object> params) {
		return Optional.ofNullable(queryForObjectOrNullInternal(sql, rowMapper, params));
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object param) {
		return queryForObjectListInternal(sql, rowMapper, Arrays.asList(param));
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
		return queryForObjectListInternal(sql, rowMapper, Arrays.asList(params));
	}

	public void execute(String sql) {
		try (Connection conn = dataSource.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.execute();
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	public boolean resultsExists(String sql, Object param) {
		return resultsExists(sql, Arrays.asList(param));
	}

	public boolean resultsExists(String sql, Object... params) {
		return resultsExists(sql, Arrays.asList(params));
	}

	public boolean resultsExists(String sql, List<Object> params) {
		try (Connection conn = dataSource.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
			SqlParams.setParams(pst, params);
			try (ResultSet resultSet = pst.executeQuery()) {
				return resultSet.next();
			}

		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	public int update(String sql, Object param) {
		return update(sql, Arrays.asList(param));
	}

	public int update(String sql, Object... params) {
		return update(sql, Arrays.asList(params));
	}

	public int update(String sql, List<Object> params) {
		try (Connection conn = dataSource.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
			SqlParams.setParams(pst, params);
			return pst.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	private <T> T queryForObjectOrNullInternal(String sql, RowMapper<T> rowMapper, List<Object> params) {
		List<T> results = queryForObjectListInternal(sql, rowMapper, params);
		if (results.isEmpty()) {
			return null;
		}

		if (results.size() > 1) {
			throw new PersistenceException("Expected one result got " + results.size());
		}

		return results.get(0);
	}

	private <T> List<T> queryForObjectListInternal(String sql, RowMapper<T> rowMapper, List<Object> params) {
		try (Connection conn = dataSource.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
			SqlParams.setParams(pst, params);
			try (ResultSet resultSet = pst.executeQuery()) {
				ResultSetAdapter adapter = new ResultSetAdapter(resultSet);
				List<T> results = new ArrayList<>();
				while (adapter.next()) {
					results.add(rowMapper.mapRow(adapter));
				}

				return results;
			}
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage(), e);
		}
	}
}
