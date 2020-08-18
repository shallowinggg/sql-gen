package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.Environment;

/**
 * Interface to be implemented by any bean that wishes to be notified
 * of the {@link Environment} that it runs in.
 *
 * @author Chris Beams
 * @since 3.1
 */
@FunctionalInterface
public interface EnvironmentAware {

    /**
     * Set the {@code Environment} that this component runs in.
     */
    void setEnvironment(Environment environment);
}
