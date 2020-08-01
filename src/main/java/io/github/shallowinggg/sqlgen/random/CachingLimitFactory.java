package io.github.shallowinggg.sqlgen.random;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Caching implementation of the {@link LimitFactory} interface,
 * caching a {@link Limit} instance per {@link LimitCacheKey}.
 * The cache key is composed of the type parameter of {@code Limit}
 * and limited value.
 *
 * @author ding shimin
 * @since 1.0
 */
public class CachingLimitFactory extends SimpleLimitFactory {

    // TODO: use limited size cache

    private static final ConcurrentMap<LimitCacheKey, Limit<?>> CACHE = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public <T> Limit<T> getLimit(Class<T> clazz, T limit) {
        LimitCacheKey key = new LimitCacheKey(clazz, limit);
        @SuppressWarnings("unchecked")
        Limit<T> tLimit = (Limit<T>) CACHE.get(key);
        if (tLimit == null) {
            tLimit = super.getLimit(clazz, limit);
            CACHE.put(key, tLimit);
        }
        return tLimit;
    }

    private static class LimitCacheKey {
        private final Class<?> clazz;
        private final Object value;

        LimitCacheKey(Class<?> clazz, Object value) {
            this.clazz = clazz;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LimitCacheKey that = (LimitCacheKey) o;
            return Objects.equals(clazz, that.clazz) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, value);
        }
    }


}
