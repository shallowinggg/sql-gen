package io.github.shallowinggg.sqlgen.random;

/**
 * Interface that represent a limit value for specified type.
 * It is mainly used for {@link LimitedRandomizer} that the
 * random value generated must not exceed this limit.
 *
 * @author ding shimin
 * @since 1.0
 */
public interface Limit<T> {

    /**
     * Return the limit value, aka the max value.
     *
     * @return limit value
     */
    T getLimit();
}
