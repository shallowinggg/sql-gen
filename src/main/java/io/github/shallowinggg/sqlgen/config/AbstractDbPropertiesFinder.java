package io.github.shallowinggg.sqlgen.config;

/**
 * @author ding shimin
 */
public abstract class AbstractDbPropertiesFinder implements DbPropertiesFinder {

    private String searchLocations;

    protected AbstractDbPropertiesFinder(String searchLocations, String searchNames) {
        this.searchLocations = searchLocations;
    }



}
