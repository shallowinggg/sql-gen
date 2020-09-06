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
 * The convenient utility class which used to read jdbc metadata.
 *
 * @author ding shimin
 */
public class JdbcSupport {

    private static final Set<Integer> SUPPORT_DEFAULT_TYPES;

    private static volatile DatabaseMetaData databaseCache;

    static {
        SUPPORT_DEFAULT_TYPES = new HashSet<>();

        // only support common types currently
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

    /**
     * Read {@link DatabaseMetaData} with default connection which get
     * from {@link io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory}.
     *
     * @return database metadata instance
     * @see Connection#getMetaData()
     */
    public static DatabaseMetaData readDatabaseMetaData() {
        DatabaseMetaData dmd = databaseCache;
        if (dmd == null) {
            synchronized (JdbcSupport.class) {
                dmd = databaseCache;
                if (dmd == null) {
                    ConnectionFactory connectionFactory = ConnectionFactory.getInstance();
                    Connection connection = null;
                    try {
                        connection = connectionFactory.createConnection();
                        dmd = connection.getMetaData();
                        databaseCache = dmd;
                    } catch (SQLException e) {
                        throw new JdbcException("Unexpected problem when read database metadata", e);
                    } finally {
                        if (connection != null) {
                            connectionFactory.recycle(connection);
                        }
                    }
                }
            }
        }
        return dmd;
    }

    /**
     * Check if the given table exists with global connection.
     * If there is a {@link SQLException SQLException} thrown,
     * it will be wrapped as a {@link RuntimeException}.
     *
     * @param tableName the name of table to check
     * @return {@code true} if the given table exists
     */
    public static boolean isTableExist(String tableName) {
        Assert.hasText(tableName, "tableName must has text");

        DatabaseMetaData databaseMetaData = readDatabaseMetaData();
        try (ResultSet table = databaseMetaData.getTables(null, null, tableName, null)) {
            return table.next();
        } catch (SQLException e) {
            throw new JdbcException(String.format("Unexpected problem when read table [%s] metadata",
                    tableName), e);
        }
    }

    /**
     * Read basic column metadata for given table. This method will
     * return necessary infos like column name, column type etc.
     *
     * @param tableName the table to search
     * @return column metadata list
     * @see DatabaseMetaData#getIndexInfo(String, String, String, boolean, boolean)
     * @see DatabaseMetaData#getColumns(String, String, String, String)
     */
    public static List<ColumnMetaData> readColumnMetaData(String tableName) {
        Assert.hasText(tableName, "tableName must has text");

        DatabaseMetaData databaseMetaData = readDatabaseMetaData();
        List<ColumnMetaData> columns = new ArrayList<>();
        try {
            // get unique constraint columns
            Set<String> uniqueColumns = new HashSet<>();
            ResultSet indexInfo = databaseMetaData.getIndexInfo(null, null, tableName, true, false);
            while (indexInfo.next()) {
                String columnName = indexInfo.getString(ColumnDescription.COLUMN_NAME);
                uniqueColumns.add(columnName);
            }

            // get column infos
            ResultSet rs = databaseMetaData.getColumns(null, "%", tableName, "%");
            while (rs.next()) {
                String name = rs.getString(ColumnDescription.COLUMN_NAME);
                int sqlType = rs.getInt(ColumnDescription.COLUMN_TYPE);
                String defaultValue = rs.getString(ColumnDescription.COLUMN_DEFAULT);
                int size = rs.getInt(ColumnDescription.COLUMN_SIZE);
                int digits = rs.getInt(ColumnDescription.DECIMAL_DIGITS);
                boolean isNullable = ColumnNullable.isNullable(rs.getInt(ColumnDescription.COLUMN_NULLABLE));
                boolean isAutoIncrement = ColumnAutoIncrement.isAutoIncrement(rs.getString(ColumnDescription.COLUMN_AUTOINCREMENT));

                columns.add(ColumnMetaData.of(name, sqlType, defaultValue, size, digits,
                        isNullable, isAutoIncrement, uniqueColumns.contains(name)));
            }
        } catch (SQLException e) {
            throw new JdbcException("Unexpected problem when read column metadata for table " + tableName, e);
        }

        return columns;
    }

    /**
     * Determine whether the given sql type can use default value which read
     * from method {@link DatabaseMetaData#getColumns(String, String, String, String)}.
     *
     * @param sqlType the JDBC type code to check
     * @return {@code true} if the sql type support default value,
     * or {@code false} otherwise.
     * @see DatabaseMetaData#getColumns(String, String, String, String)
     * @see #SUPPORT_DEFAULT_TYPES
     */
    public static boolean supportDefaultValue(int sqlType) {
        return SUPPORT_DEFAULT_TYPES.contains(sqlType);
    }

}
