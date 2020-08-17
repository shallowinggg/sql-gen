package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;

/**
 * Interface that used to find {@link DbConfig}.
 *
 * @author ding shimin
 */
@FunctionalInterface
public interface DbConfigFinder {

    /**
     * Post-process the given {@code environment}.
     *
     * @param environment    the environment to post-process
     * @param resourceLoader the resource loader that application use
     */
    DbConfig find(ConfigurableEnvironment environment, ResourceLoader resourceLoader);
}
