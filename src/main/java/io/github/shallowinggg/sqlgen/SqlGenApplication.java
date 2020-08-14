package io.github.shallowinggg.sqlgen;

import io.github.shallowinggg.sqlgen.config.DbConfig;
import io.github.shallowinggg.sqlgen.config.EnvironmentPostProcessor;
import io.github.shallowinggg.sqlgen.config.ColumnConfig;
import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.env.StandardEnvironment;
import io.github.shallowinggg.sqlgen.io.DefaultResourceLoader;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;
import io.github.shallowinggg.sqlgen.jdbc.ColumnMetaData;
import io.github.shallowinggg.sqlgen.jdbc.JdbcReader;
import io.github.shallowinggg.sqlgen.jdbc.JdbcWriter;
import io.github.shallowinggg.sqlgen.jdbc.SimpleJdbcReader;
import io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory;

import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * @author ding shimin
 */
public class SqlGenApplication {

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    private ConfigurableEnvironment environment = new StandardEnvironment();

    private EnvironmentPostProcessor environmentPostProcessor;

    private DbConfig dbConfig;

    private List<ColumnConfig> columnConfigs;

    public void run() {
        if(dbConfig == null) {
            environmentPostProcessor.postProcessEnvironment(environment, this);
        }
        ConnectionFactory.init(dbConfig);
        JdbcReader jdbcReader = new SimpleJdbcReader();
        DatabaseMetaData databaseMetaData = jdbcReader.readDatabaseMetaData();
        List<ColumnMetaData> columnMetaDataList = jdbcReader.readColumnMetaData("");
        JdbcWriter jdbcWriter;

    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }
}
