package io.github.shallowinggg.sqlgen;

/**
 * The base exception type for sql-gen exceptions.
 *
 * @author ding shimin
 */
public class SqlGenException extends RuntimeException {

    /**
     * Constructs a SqlGenException using the given message and underlying cause.
     *
     * @param cause The underlying cause.
     */
    public SqlGenException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
