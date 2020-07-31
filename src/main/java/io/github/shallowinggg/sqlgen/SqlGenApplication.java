package io.github.shallowinggg.sqlgen;

import io.github.shallowinggg.sqlgen.config.DbConfig;
import io.github.shallowinggg.sqlgen.config.TableConfig;
import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;

import java.util.List;

/**
 * @author ding shimin
 */
public class SqlGenApplication {

    private ResourceLoader resourceLoader;

    private ConfigurableEnvironment environment;

    private DbConfig dbConfig;

    private List<TableConfig> tableConfigs;

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
