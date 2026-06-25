package se.uhr.simone.atom.feed.utils.jdbc;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

public class ResultSetAdapter implements AutoCloseable {

	private static final TimeZone UTC_TZ = TimeZone.getTimeZone("UTC");

	private final ResultSet resultSet;

	public ResultSetAdapter(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	boolean next() {
		try {
			return resultSet.next();
		} catch (SQLException e) {
			throw createWrappedException(e);
		}
	}

	@Override
	public void close() {
		try {
			resultSet.close();
		} catch (SQLException e) {
			throw createWrappedException(e);
		}
	}

	public String getString(String column) {
		String value = tryGetValue(column, resultSet::getString);
		return value != null ? value.trim() : null;
	}

	public int getInt(String column) {
		return tryGetValue(column, resultSet::getInt);
	}

	public short getShort(String column) {
		return tryGetValue(column, resultSet::getShort);
	}

	public long getLong(String column) {
		return tryGetValue(column, resultSet::getLong);
	}

	public BigDecimal getBigDecimal(String column) {
		return tryGetValue(column, resultSet::getBigDecimal);
	}

	public Timestamp getTimestamp(String column) {
		return tryGetValue(column, resultSet::getTimestamp);
	}

	public Timestamp getTimestampUTC(String column) {
		Calendar utcCalendar = Calendar.getInstance(UTC_TZ);
		try {
			return resultSet.getTimestamp(column, utcCalendar);
		} catch (SQLException e) {
			throw createWrappedException(e);
		}
	}

	public Instant getInstant(String column) {
		Timestamp timestamp = tryGetValue(column, resultSet::getTimestamp);
		return timestamp != null ? timestamp.toInstant() : null;
	}

	public boolean wasNull() {
		try {
			return resultSet.wasNull();
		} catch (SQLException e) {
			throw createWrappedException(e);
		}
	}

	public Object getObject(String column) {
		return tryGetValue(column, resultSet::getObject);
	}

	private <T> T tryGetValue(String column, ResultRow<T> row) {
		try {
			return row.getValueFrom(column);
		} catch (SQLException e) {
			throw createWrappedException(e);
		}
	}

	private interface ResultRow<T> {

		T getValueFrom(String column) throws SQLException;

	}

	private PersistenceException createWrappedException(SQLException e) {
		return new PersistenceException(e.getMessage(), e);
	}
}
