package io.github.shallowinggg.sqlgen.random;

/**
 * Interface that used to generate random value for
 * specified type.
 *
 * @author ding shimin
 * @since 1.0
 */
public interface Randomizer<T> {

    /**
     * Returns the next pseudorandom value.
     *
     * @return random value
     */
    T nextValue();
}
