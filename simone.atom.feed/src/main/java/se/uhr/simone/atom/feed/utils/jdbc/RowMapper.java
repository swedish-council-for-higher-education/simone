package se.uhr.simone.atom.feed.utils.jdbc;

@FunctionalInterface
public interface RowMapper<T> {

	T mapRow(ResultSetAdapter rs);

}
