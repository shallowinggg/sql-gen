package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.SqlGenApplication;
import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.env.Environment;

/**
 * Allows for customization of the application's {@link Environment} prior to the
 * application context being refreshed.
 * <p>
 * EnvironmentPostProcessor implementations have to be registered in
 * {@code META-INF/spring.factories}, using the fully qualified name of this class as the
 * key.
 * <p>
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author ding shimin
 */
@FunctionalInterface
public interface EnvironmentPostProcessor {

    /**
     * Post-process the given {@code environment}.
     * @param environment the environment to post-process
     * @param sqlGenApplication the resource loader that application use
     */
    void postProcessEnvironment(ConfigurableEnvironment environment, SqlGenApplication sqlGenApplication);
}
