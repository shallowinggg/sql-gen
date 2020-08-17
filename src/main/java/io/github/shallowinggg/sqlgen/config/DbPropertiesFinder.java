package io.github.shallowinggg.sqlgen.config;

import java.util.List;

/**
 * Interface that used to provide basic infos for finding db configurations.
 * <p>
 * DbPropertiesFinder implementations have to be registered in
 * {@code META-INF/spring.factories}, using the fully qualified name of
 * this class as the key.
 *
 * @author ding shimin
 * @see SpringBootDbPropertiesFinder
 * @see DefaultDbPropertiesFinder
 * @since 1.0
 */
public interface DbPropertiesFinder {

    /**
     * Determine whether this finder is a candidate. This method is mainly
     * used to optimize if this finder won't be used in current environment.
     * For example, {@link SpringBootDbPropertiesFinder} will return false if
     * not use SpringBoot in current environment.
     *
     * @return {@code true} if this finder will be used in current environment,
     * or {@code false} otherwise.
     */
    boolean isCandidate();

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

    /**
     * Return {@link DbConfigProperties}s which will be used to find
     * db configuration.
     *
     * @return DbConfigProperties list
     */
    List<DbConfigProperties> getDbConfigProperties();

}
