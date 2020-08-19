package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ding shimin
 */
public class ShortRandomizer implements Randomizer<Short> {

    private static final int DEFAULT_MAX_VALUE = Short.MAX_VALUE + 1;

    private final short maxValue;

    public static ShortRandomizer create() {
        return new ShortRandomizer();
    }

    public static ShortRandomizer create(int maxValue) {
        return new ShortRandomizer(maxValue);
    }

    public ShortRandomizer() {
        this(DEFAULT_MAX_VALUE);
    }

    public ShortRandomizer(int maxValue) {
        Assert.isTrue(maxValue > 0 && maxValue <= DEFAULT_MAX_VALUE,
                "max value must not out of range, expected: (0, 32768], actual: " + maxValue);
        this.maxValue = (short) maxValue;
    }

    @Override
    public Short nextValue() {
        return (short) ThreadLocalRandom.current().nextInt(maxValue);
    }
}
