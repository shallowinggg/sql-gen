package io.github.shallowinggg.sqlgen.config;

/**
 * @author ding shimin
 */
public interface DbConfigProperties {

    boolean isCandicate();

    String getUrlPropertyName();

    String getDriverNamePropertyName();

    String getUsernamePropertyName();

    String getPasswordPropertyName();
}
