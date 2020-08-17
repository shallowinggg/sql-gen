package io.github.shallowinggg.sqlgen.config;

import java.util.List;

/**
 * @author ding shimin
 */
public interface DbPropertiesFinder {

    /**
     * Return search locations for the config files. Each search
     * location should be a directory path (ending in "/") and it
     * will be the prefixed by the file names constructed from
     * {@link #getSearchNames() search names}.
     *
     * @return the search locations
     */
    List<String> getSearchLocations();

    /**
     * Return the names of the files that should be loaded (excluding
     * file extension).
     *
     * @return the file names
     */
    List<String> getSearchNames();

    List<DbConfigProperties> getDbConfigProperties();

    /**
     * Determine whether this finder is a candidate. This method is mainly
     * used to optimize if this finder won't be used in current environment.
     * For example, {@link SpringBootDbPropertiesFinder} will return false if
     * don't use springboot in current environment.
     *
     * @return {@code true} if this finder will be used in current environment,
     * or {@code false} otherwise.
     */
    boolean isCandidate();

}
