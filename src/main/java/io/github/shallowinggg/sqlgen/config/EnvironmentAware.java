package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.Environment;

/**
 * @author ding shimin
 */
@FunctionalInterface
public interface EnvironmentAware {

    void setEnvironment(Environment environment);
}
