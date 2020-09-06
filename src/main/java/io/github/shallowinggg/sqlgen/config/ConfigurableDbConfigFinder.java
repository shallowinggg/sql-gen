package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;

import java.util.List;

/**
 * Configuration interface to be implemented by most if not all
 * {@link DbConfigFinder} types. Provides facilities for setting
 * {@link DbPropertyConfig}s and customize environment and
 * resource loader.
 *
 * @author ding shimin
 */
public interface ConfigurableDbConfigFinder extends DbConfigFinder {

    /**
     * Set the {@link ConfigurableEnvironment} to be used when performing
     * properties find and resolve.
     * <p><strong>Note:</strong> as an alternative to fully replacing the
     * {@code ConfigurableEnvironment}, consider adding or removing
     * individual {@code Profile} by drilling into {@link #getEnvironment()}
     * and calling methods such as {@code #addActiveProfile}.
     *
     * @param environment the environment instance to set
     * @see #getEnvironment()
     * @see ConfigurableEnvironment#addActiveProfile(String)
     * @see ConfigurableEnvironment#setActiveProfiles(String...)
     */
    void setEnvironment(ConfigurableEnvironment environment);

    /**
     * Return the {@link ConfigurableEnvironment} used when performing properties
     * find and resolve.
     * <p>The configurable nature of the returned environment allows for
     * the convenient addition and removal of individual {@code Profile}:
     * <pre class="code">
     * ConfigurableEnvironment environment = finder.getEnvironment();
     * environment.addActiveProfile("dev");
     * </pre>
     *
     * @return the {@link ConfigurableEnvironment} instance
     * @see ConfigurableEnvironment#addActiveProfile(String)
     * @see ConfigurableEnvironment#setActiveProfiles(String...)
     */
    ConfigurableEnvironment getEnvironment();

    /**
     * Set the {@link ResourceLoader} to be used when performing
     * resource load like file.
     *
     * @param resourceLoader the {@code ResourceLoader} instance to set
     * @see #getResourceLoader()
     */
    void setResourceLoader(ResourceLoader resourceLoader);

    /**
     * Return the {@link ResourceLoader} to be used when performing
     * resource load like file.
     *
     * @return the {@code ResourceLoader} instance
     * @see #getResourceLoader()
     */
    ResourceLoader getResourceLoader();

    /**
     * Add a new {@code DbPropertyConfig}. If the given
     * {@code DbPropertyConfig} is exist (determine by
     * {@link DbPropertyConfig#name()}), the old config will
     * be replaced by the new one.
     *
     * @param property the {@code DbPropertyConfig} to be added
     * @throws IllegalArgumentException if the given {@code DbPropertyConfig} name is invalid
     */
    void addDbPropertyConfig(DbPropertyConfig property);

    /**
     * Specify the set of {@link DbPropertyConfig} instances for this finder.
     * {@code DbPropertyConfig} will be used after load all available
     * properties files.
     *
     * @param dbPropertyConfigs the set of {@code DbPropertyConfig} instances
     * @throws IllegalArgumentException if the given {@code DbPropertyConfig} name is invalid
     */
    void setDbPropertyConfigs(List<DbPropertyConfig> dbPropertyConfigs);
}
