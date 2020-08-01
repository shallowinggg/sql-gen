package io.github.shallowinggg.sqlgen.random;

/**
 * Sub interface for {@link Randomizer}. Provides facility that
 * generate random value under given {@link Limit limit}.
 *
 * @author ding shimin
 * @since 1.0
 */
public interface LimitedRandomizer<T, U> extends Randomizer<T> {

    /**
     * Returns the next pseudorandom value that under the given
     * {@link Limit limit}.
     *
     * @param limit the max value that generated random value
     *              must not exceed
     * @return random value
     */
    T nextValue(Limit<U> limit);
}
