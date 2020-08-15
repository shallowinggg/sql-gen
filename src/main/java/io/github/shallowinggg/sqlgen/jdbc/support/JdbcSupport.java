package io.github.shallowinggg.sqlgen.jdbc.support;

import io.github.shallowinggg.sqlgen.JdbcException;
import io.github.shallowinggg.sqlgen.jdbc.ColumnMetaData;
import io.github.shallowinggg.sqlgen.util.Assert;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ding shimin
 */
public class JdbcSupport {

    private static final Set<Integer> SUPPORT_DEFAULT_TYPES;

    static {
        SUPPORT_DEFAULT_TYPES = new HashSet<>();

        SUPPORT_DEFAULT_TYPES.add(Types.BIT);
        SUPPORT_DEFAULT_TYPES.add(Types.BOOLEAN);
        SUPPORT_DEFAULT_TYPES.add(Types.TINYINT);
        SUPPORT_DEFAULT_TYPES.add(Types.SMALLINT);
        SUPPORT_DEFAULT_TYPES.add(Types.NUMERIC);
        SUPPORT_DEFAULT_TYPES.add(Types.INTEGER);
        SUPPORT_DEFAULT_TYPES.add(Types.BIGINT);

        SUPPORT_DEFAULT_TYPES.add(Types.CHAR);
        SUPPORT_DEFAULT_TYPES.add(Types.NCHAR);
        SUPPORT_DEFAULT_TYPES.add(Types.VARCHAR);
        SUPPORT_DEFAULT_TYPES.add(Types.NVARCHAR);
        SUPPORT_DEFAULT_TYPES.add(Types.LONGVARCHAR);
        SUPPORT_DEFAULT_TYPES.add(Types.LONGNVARCHAR);

        SUPPORT_DEFAULT_TYPES.add(Types.REAL);
        SUPPORT_DEFAULT_TYPES.add(Types.FLOAT);
        SUPPORT_DEFAULT_TYPES.add(Types.DOUBLE);
        SUPPORT_DEFAULT_TYPES.add(Types.DECIMAL);
    }

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
                int sqlType = rs.getInt(ColumnDescription.COLUMN_TYPE);
                String type = JdbcTypeNameMapper.getTypeName(rs.getInt(ColumnDescription.COLUMN_TYPE));
                String defaultValue = rs.getString(ColumnDescription.COLUMN_DEFAULT);
                int size = rs.getInt(ColumnDescription.COLUMN_SIZE);
                boolean isNullable = ColumnNullable.isNullable(rs.getInt(ColumnDescription.COLUMN_NULLABLE));
                boolean isAutoIncrement = ColumnAutoIncrement.isAutoIncrement(rs.getString(ColumnDescription.COLUMN_AUTOINCREMENT));

                columns.add(ColumnMetaData.of(name, sqlType, type, defaultValue, size, isNullable, isAutoIncrement));
            }
        } catch (SQLException e) {
            throw new JdbcException("Unexpected problem when read column metadata for table " + tableName, e);
        }

        return columns;
    }

    public static boolean supportDefaultValue(int sqlType) {
        return SUPPORT_DEFAULT_TYPES.contains(sqlType);
    }

}
