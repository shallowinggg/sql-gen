package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;

/**
 * Interface that used to find {@link DbConfig}.
 * <p>
 * EnvironmentPostProcessor implementations have to be registered in
 * {@code META-INF/spring.factories}, using the fully qualified name of this class as the
 * key.
 * <p>
 *
 * @author ding shimin
 */
@FunctionalInterface
public interface DbConfigFinder {

    /**
     * Post-process the given {@code environment}.
     * @param environment the environment to post-process
     * @param resourceLoader the resource loader that application use
     */
    DbConfig find(ConfigurableEnvironment environment, ResourceLoader resourceLoader);
}
