package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.Environment;

/**
 * @author ding shimin
 */
public interface ConfigurablePropertiesFinder extends PropertiesFinder {

    Environment getEnvironment();

    void setEnvironment(Environment environment);
}
