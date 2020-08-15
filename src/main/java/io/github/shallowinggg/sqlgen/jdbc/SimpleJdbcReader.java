package io.github.shallowinggg.sqlgen.jdbc;

import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * @author ding shimin
 */
public class SimpleJdbcReader implements JdbcReader {

    @Override
    public DatabaseMetaData readDatabaseMetaData() {
        return null;
    }

    @Override
    public List<ColumnMetaData> readColumnMetaData(String tableName) {
        return null;
    }

}
