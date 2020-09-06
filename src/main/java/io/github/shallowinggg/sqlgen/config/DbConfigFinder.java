package io.github.shallowinggg.sqlgen.config;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface that used to find {@link DbConfig}.
 * <p>
 * Implementations must provide necessary {@code DbPropertyConfig}s
 * which used to search {@code DbConfig} by {@code ConfigurableEnvironment}.
 *
 * @author ding shimin
 * @see SpringBootDbConfigFinder
 * @see DefaultDbConfigFinder
 * @since 1.0
 */
public interface DbConfigFinder {

    /**
     * Find {@code DbConfig} with the given {@code environment} and
     * {@code resourceLoader}.
     *
     * @return {@code DbConfig} instance if find
     */
    @Nullable
    DbConfig find();

    /**
     * Find a {@code DbPropertyConfig} by its name.
     *
     * @param name the name of {@code DbPropertyConfig}
     * @return {@code DbPropertyConfig} instance if exist
     */
    @Nullable
    DbPropertyConfig getDbPropertyConfig(String name);

    /**
     * Find all {@code DbPropertyConfig} instances. If none,
     * return an empty list.
     *
     * @return all {@code DbPropertyConfig} instances
     */
    List<DbPropertyConfig> getAllDbPropertyConfigs();


    /**
     * DataSource property configuration
     */
    interface DbPropertyConfig {

        /**
         * Determine whether this config is a candidate. This method is mainly
         * used to optimize if this config won't be used in current environment.
         * For example, {@code C3p0DbPropertyConfig} will return false if
         * C3p0 is not in classpath.
         *
         * @return {@code true} if this config will be used in current environment,
         * or {@code false} otherwise.
         */
        boolean isCandidate();

        /**
         * The unique identifier for this config.
         *
         * @return the unique identifier
         */
        String name();

        /**
         * The name of datasource property url.
         *
         * @return name of datasource property url
         */
        String getUrlPropertyName();

        /**
         * The name of datasource property driver name.
         *
         * @return name of datasource property driver name.
         */
        String getDriverNamePropertyName();

        /**
         * The name of datasource property username.
         *
         * @return name of datasource property username
         */
        String getUsernamePropertyName();

        /**
         * The name of datasource property password.
         *
         * @return name of datasource property password
         */
        String getPasswordPropertyName();
    }
}
