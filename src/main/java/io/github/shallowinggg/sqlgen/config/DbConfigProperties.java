package io.github.shallowinggg.sqlgen.config;

/**
 * @author ding shimin
 */
public interface DbConfigProperties {

    boolean isCandidate();

    String getUrlPropertyName();

    String getDriverNamePropertyName();

    String getUsernamePropertyName();

    String getPasswordPropertyName();
}
