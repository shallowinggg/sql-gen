package io.github.shallowinggg.sqlgen.jdbc.support;

import io.github.shallowinggg.sqlgen.JdbcException;
import io.github.shallowinggg.sqlgen.jdbc.ColumnMetaData;
import io.github.shallowinggg.sqlgen.util.Assert;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ding shimin
 */
public class JdbcSupport {

    public static DatabaseMetaData readDatabaseMetaData() {
        ConnectionFactory connectionFactory = ConnectionFactory.getInstance();
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            return connection.getMetaData();
        } catch (SQLException e) {
            throw new JdbcException("Unexpected problem when read database metadata", e);
        } finally {
            if (connection != null) {
                connectionFactory.recycle(connection);
            }
        }
    }

    public static List<ColumnMetaData> readColumnMetaData(String tableName) {
        Assert.hasText(tableName, "tableName must has text");

        DatabaseMetaData databaseMetaData = readDatabaseMetaData();
        List<ColumnMetaData> columns = new ArrayList<>();
        try {
            ResultSet rs = databaseMetaData.getColumns(null, "%", tableName, "%");
            while (rs.next()) {
                String name = rs.getString(ColumnDescription.COLUMN_NAME);
                String type = JdbcTypeNameMapper.getTypeName(rs.getInt(ColumnDescription.COLUMN_TYPE));
                String defaultValue = rs.getString(ColumnDescription.COLUMN_DEFAULT);
                int size = rs.getInt(ColumnDescription.COLUMN_SIZE);
                boolean isNullable = ColumnNullable.isNullable(rs.getInt(ColumnDescription.COLUMN_NULLABLE));
                boolean isAutoIncrement = ColumnAutoIncrement.isAutoIncrement(rs.getString(ColumnDescription.COLUMN_AUTOINCREMENT));

                columns.add(ColumnMetaData.make(name, type, defaultValue, size, isNullable, isAutoIncrement));
            }
        } catch (SQLException e) {
            throw new JdbcException("Unexpected problem when read column metadata for table " + tableName, e);
        }

        return columns;
    }

}
