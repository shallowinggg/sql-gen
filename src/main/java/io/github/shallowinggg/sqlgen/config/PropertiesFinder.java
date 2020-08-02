package io.github.shallowinggg.sqlgen.config;

import java.util.List;
import java.util.Set;

/**
 * @author ding shimin
 */
public interface PropertiesFinder {

    String name();

    Set<String> getSearchLocations();

    Set<String> getSearchNames();

    List<DbConfigProperties> getDbConfigProperties();

    boolean isCandidate();

}
