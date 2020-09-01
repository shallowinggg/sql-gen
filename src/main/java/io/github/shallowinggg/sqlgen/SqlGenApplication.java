package io.github.shallowinggg.sqlgen;

import io.github.shallowinggg.sqlgen.config.ColumnConfig;
import io.github.shallowinggg.sqlgen.config.ConfigFileDbConfigFinder;
import io.github.shallowinggg.sqlgen.config.DbConfig;
import io.github.shallowinggg.sqlgen.config.DbConfigFinder;
import io.github.shallowinggg.sqlgen.config.DbPropertiesFinder;
import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.env.StandardEnvironment;
import io.github.shallowinggg.sqlgen.io.DefaultResourceLoader;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;
import io.github.shallowinggg.sqlgen.jdbc.BatchJdbcWriter;
import io.github.shallowinggg.sqlgen.jdbc.JdbcWriter;
import io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory;
import io.github.shallowinggg.sqlgen.util.CollectionUtils;

import java.util.List;

/**
 * @author ding shimin
 */
public class SqlGenApplication {

    private ResourceLoader resourceLoader;

    private ConfigurableEnvironment environment;

    private DbConfigFinder dbConfigFinder;

    private DbConfig dbConfig;

    private List<ColumnConfig> columnConfigs;

    private String table;

    private int rows;

    private List<DbPropertiesFinder> customerDbPropertiesFinder;

    public void run() {
        DbConfig dbConfig = this.dbConfig;
        if (dbConfig == null) {
            ConfigFileDbConfigFinder dbConfigFinder = new ConfigFileDbConfigFinder();
            if (CollectionUtils.isNotEmpty(customerDbPropertiesFinder)) {
                dbConfigFinder.addDbPropertiesFinders(customerDbPropertiesFinder);
            }
            if (resourceLoader == null) {
                resourceLoader = new DefaultResourceLoader();
            }
            if (environment == null) {
                this.environment = new StandardEnvironment();
            }
            dbConfig = dbConfigFinder.find(environment, resourceLoader);
            if (dbConfig == null) {
                throw new IllegalStateException("Find no db config, initialize fail");
            }
        }
        ConnectionFactory.init(dbConfig);
        JdbcWriter jdbcWriter = new BatchJdbcWriter();
        
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }
}
