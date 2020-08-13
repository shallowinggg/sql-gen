package io.github.shallowinggg.sqlgen.jdbc;

import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * @author ding shimin
 */
public interface JdbcReader {

    /**
     * Read {@link DatabaseMetaData} with default connection which get
     * from {@link io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory}.
     *
     * @return database metadata
     */
    DatabaseMetaData readDatabaseMetaData();

    /**
     * Read basic column metadata for given table. This method will
     * return necessary infos like column name, column type etc.
     *
     * @param tableName the table to search
     * @return column metadata list
     */
    List<ColumnMetaData> readColumnMetaData(String tableName);
}
