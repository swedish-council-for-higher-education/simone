package se.uhr.simone.atom.feed.utils.jdbc;

import java.io.Serial;

public class PersistenceException extends RuntimeException {

	@Serial
    private static final long serialVersionUID = 1L;

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
