package io.github.shallowinggg.sqlgen;

import io.github.shallowinggg.sqlgen.config.ColumnConfig;
import io.github.shallowinggg.sqlgen.config.DbConfig;
import io.github.shallowinggg.sqlgen.config.DbPropertiesFinder;
import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;
import io.github.shallowinggg.sqlgen.random.Randomizer;
import io.github.shallowinggg.sqlgen.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ding shimin
 */
public class SqlGenApplicationBuilder {

    private ResourceLoader resourceLoader;

    private ConfigurableEnvironment environment;

    private DbConfig dbConfig;

    private String table;

    private int rows;

    private List<ColumnConfig> columnConfigs;

    private List<DbPropertiesFinder> dbPropertiesFinders;


    public SqlGenApplicationBuilder resourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        return this;
    }

    public SqlGenApplicationBuilder environment(ConfigurableEnvironment environment) {
        this.environment = environment;
        return this;
    }

    public SqlGenApplicationBuilder dbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
        return this;
    }

    public SqlGenApplicationBuilder columnConfigs(List<ColumnConfig> columnConfigs) {
        this.columnConfigs = columnConfigs;
        return this;
    }

    public SqlGenApplicationBuilder table(String table) {
        this.table = table;
        return this;
    }

    public SqlGenApplicationBuilder rows(int rows) {
        this.rows = rows;
        return this;
    }

    public SqlGenApplicationBuilder addDbPropertiesFinder(DbPropertiesFinder finder) {
        Assert.notNull(finder, "finder must not be null");
        List<DbPropertiesFinder> finders = this.dbPropertiesFinders;
        if (finders == null) {
            finders = new ArrayList<>(5);
            this.dbPropertiesFinders = finders;
        }
        finders.add(finder);
        return this;
    }

    public DbConfigBuilder newDbConfigBuilder() {
        return new DbConfigBuilder();
    }

    public ColumnBuilder newColumnBuilder() {
        return new ColumnBuilder();
    }

    public class DbConfigBuilder {
        private String url;

        private String driverName;

        private String username;

        private String password;

        public DbConfigBuilder url(String url) {
            this.url = url;
            return this;
        }

        public DbConfigBuilder driverName(String driverName) {
            this.driverName = driverName;
            return this;
        }

        public DbConfigBuilder username(String username) {
            this.username = username;
            return this;
        }

        public DbConfigBuilder password(String password) {
            this.password = password;
            return this;
        }

        public SqlGenApplicationBuilder buildDbConfig() {
            DbConfig dbConfig = new DbConfig(url, driverName, username, password);
            if (!dbConfig.isValid()) {
                throw new IllegalArgumentException("Illegal arguments for db config");
            }
            return SqlGenApplicationBuilder.this.dbConfig(dbConfig);
        }
    }

    public class ColumnBuilder {
        private final List<ColumnConfig> columns = new ArrayList<>();

        public ColumnBuilder addColumnConfig(String columnName, Randomizer<?> randomizer) {
            Assert.hasText(columnName, "columnName must has text");
            Assert.notNull(randomizer, "restriction must not null");
            columns.add(new ColumnConfig(columnName, randomizer));
            return this;
        }

        public SqlGenApplicationBuilder buildColumnConfig() {
            return SqlGenApplicationBuilder.this.columnConfigs(columns);
        }
    }
}
