package io.github.shallowinggg.sqlgen.random;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Abstract randomizer implementation for integer type. Sub
 * class can use method {@link #nextInt()} to generate a
 * integer value.
 *
 * @author ding shimin
 * @see ByteRandomizer
 * @see ShortRandomizer
 * @see IntegerRandomizer
 * @since 1.0
 */
public abstract class AbstractIntegerRandomizer<T> extends AbstractTypedRandomizer<T> {
    private final int maxValue;

    protected AbstractIntegerRandomizer(int maxValue) {
        this.maxValue = maxValue;
    }

    public int nextInt() {
        return ThreadLocalRandom.current().nextInt(maxValue);
    }

    /**
     * Convenient check method for sub class. The given num
     * must follow range [min, max] which supplied by the latter
     * two parameters, otherwise {@link IllegalArgumentException}
     * will be thrown.
     *
     * @param num the num to check
     * @param min the min value
     * @param max the max value
     * @return the num itself if check successfully
     * @throws IllegalArgumentException if {@code num} out of range
     */
    public static int rangeCheck(int num, int min, int max) {
        if (num < min || num > max) {
            throw new IllegalArgumentException(String.format("num must not out of range, expected: [%d, %d]," +
                    "actual: %d", min, max, num));
        }
        return num;
    }
}
