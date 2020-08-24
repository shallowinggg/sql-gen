package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.jdbc.ColumnMetaData;

/**
 * Factory interface for {@link Randomizer} instances.
 * Allows for caching a Limit per original resource.
 *
 * @author ding shimin
 * @since 1.0
 */
public interface RandomizerFactory {

    /**
     * Obtain a {@link Randomizer} instance for the given metadata.
     * If no implementation for given type, return {@code null}.
     *
     * @param column metadata of table column
     * @return a {@code Randomizer} instance or null if no implementation
     */
    Randomizer<?> getRandomizer(ColumnMetaData column);
}
