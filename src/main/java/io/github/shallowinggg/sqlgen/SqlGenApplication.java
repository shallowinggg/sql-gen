package io.github.shallowinggg.sqlgen;

import io.github.shallowinggg.sqlgen.config.ColumnConfig;
import io.github.shallowinggg.sqlgen.config.DbConfig;
import io.github.shallowinggg.sqlgen.config.DbConfigFinder;
import io.github.shallowinggg.sqlgen.config.DefaultDbConfigFinder;
import io.github.shallowinggg.sqlgen.config.SpringBootDbConfigFinder;
import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;
import io.github.shallowinggg.sqlgen.jdbc.BatchJdbcWriter;
import io.github.shallowinggg.sqlgen.jdbc.AbstractJdbcWriter;
import io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory;

import java.util.ArrayList;
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

    private List<DbConfigFinder> customerDbConfigFinder;

    public void run() {
        DbConfig dbConfig = getOrFindDbConfig();

        ConnectionFactory.init(dbConfig);
        AbstractJdbcWriter jdbcWriter = new BatchJdbcWriter();

    }

    private DbConfig getOrFindDbConfig() {
        DbConfig dbConfig = this.dbConfig;
        if (dbConfig == null) {
            List<DbConfigFinder> finders = new ArrayList<>(customerDbConfigFinder);
            finders.add(new SpringBootDbConfigFinder());
            finders.add(new DefaultDbConfigFinder());
            for (DbConfigFinder finder : finders) {
                DbConfig result = finder.find();
                if (result != null) {
                    return result;
                }
            }
        }
        throw new IllegalStateException("Find no db config, initialize fail");
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }
}
