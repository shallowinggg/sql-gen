package io.github.shallowinggg.sqlgen.config;

/**
 * @author ding shimin
 */
public abstract class AbstractPropertiesFinder implements PropertiesFinder {

    private String searchLocations;

    protected AbstractPropertiesFinder(String searchLocations, String searchNames) {
        this.searchLocations = searchLocations;
    }



}
