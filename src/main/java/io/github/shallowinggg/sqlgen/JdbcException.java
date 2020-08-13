package io.github.shallowinggg.sqlgen;

import java.sql.SQLException;

/**
 * The common exception used to wrap all {@link SQLException}s.
 *
 * @author ding shimin
 */
public class JdbcException extends SqlGenException {

    /**
     * Constructs a JdbcException using the given message and underlying cause.
     *
     * @param cause The underlying cause.
     */
    public JdbcException(String msg, SQLException cause) {
        super(msg, cause);
    }
}
