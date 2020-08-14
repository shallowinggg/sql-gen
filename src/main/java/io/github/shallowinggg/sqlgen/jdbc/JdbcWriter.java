package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.JdbcException;
import io.github.shallowinggg.sqlgen.SqlGenException;
import io.github.shallowinggg.sqlgen.config.ColumnConfig;
import io.github.shallowinggg.sqlgen.jdbc.support.JdbcSupport;
import io.github.shallowinggg.sqlgen.random.Randomizer;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author ding shimin
 */
public class JdbcWriter {


    private List<ColumnConfig> columnConfigs;

    private Map<String, ColumnConfig> columnConfigMap;

    public JdbcWriter(List<ColumnConfig> columnConfigs) {
        this.columnConfigs = columnConfigs;
    }

    public void execute() throws SQLException {
        DatabaseMetaData databaseMetaData = JdbcSupport.readDatabaseMetaData();
        if(databaseMetaData.isReadOnly()) {
            throw new SqlGenException("Database is read only");
        }
        List<ColumnMetaData> columnMetaDataList = JdbcSupport.readColumnMetaData("");
        for(ColumnMetaData column : columnMetaDataList) {
            Randomizer<?> randomizer = columnConfigMap.get(column.getName()).getRestriction().randomizer();
            if(randomizer == null) {
                // create type matching randomizer
            }


        }
    }
}
