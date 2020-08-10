package io.github.shallowinggg.sqlgen.jdbc.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author ding shimin
 */
public class JdbcSupport {

    public static DatabaseMetaData findDatabaseMetaData() {
        ConnectionFactory connectionFactory = ConnectionFactory.getInstance();
        try {
            Connection c = connectionFactory.createConnection();
            return c.getMetaData();
        } catch (SQLException e) {
            return null;
        }
    }
}
