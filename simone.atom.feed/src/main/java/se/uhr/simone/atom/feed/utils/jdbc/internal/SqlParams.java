package se.uhr.simone.atom.feed.utils.jdbc.internal;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface SqlParams {

	static void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		int index = 1;
		for (Object param : params) {
			setParamAtIndex(ps, param, index);
			index++;
		}
	}

	private static void setParamAtIndex(PreparedStatement ps, Object param, int index) throws SQLException {
        switch (param) {
            case null -> ps.setNull(index, ps.getParameterMetaData().getParameterType(index));
            case String s -> ps.setString(index, s);
            case Timestamp timestamp -> ps.setTimestamp(index, timestamp);
            case Boolean b -> ps.setBoolean(index, (boolean) param);
            case Short i -> ps.setShort(index, (short) param);
            case Integer i -> ps.setInt(index, (int) param);
            case Long l -> ps.setLong(index, (long) param);
            case BigDecimal bigDecimal -> ps.setBigDecimal(index, bigDecimal);
            case byte[] bytes -> ps.setBytes(index, bytes);
            case LocalTime localTime -> ps.setTime(index, Time.valueOf(localTime));
            case LocalDate localDate -> ps.setDate(index, Date.valueOf(localDate));
            case LocalDateTime localDateTime -> ps.setTimestamp(index, Timestamp.valueOf(localDateTime));
            case Instant instant -> ps.setTimestamp(index, Timestamp.from(instant));
            default -> ps.setObject(index, param);
        }
	}
}
