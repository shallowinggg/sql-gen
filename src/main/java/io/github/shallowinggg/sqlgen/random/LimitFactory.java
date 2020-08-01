package io.github.shallowinggg.sqlgen.random;

import javax.annotation.Nullable;

/**
 * Factory interface for {@link Limit} instances.
 * Allows for caching a Limit per original resource.
 *
 * @author ding shimin
 * @see SimpleLimitFactory
 * @see CachingLimitFactory
 * @since 1.0
 */
public interface LimitFactory {

    /**
     * Obtain a {@link Limit} instance for the given type
     * and limit value. If no implementation for given
     * type, return {@code null}.
     *
     * @param clazz the type of Limit
     * @param limit the limit value of Limit
     * @param <T>   type parameter of Limit
     * @return a Limit instance or null if no implementation
     */
    @Nullable
    <T> Limit<T> getLimit(Class<T> clazz, T limit);
}
