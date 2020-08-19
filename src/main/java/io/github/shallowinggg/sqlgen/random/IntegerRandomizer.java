package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ding shimin
 */
public class IntegerRandomizer implements Randomizer<Integer> {

    private static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;

    private final int maxValue;

    public static IntegerRandomizer create() {
        return new IntegerRandomizer();
    }

    public static IntegerRandomizer create(int maxValue) {
        return new IntegerRandomizer(maxValue);
    }

    public IntegerRandomizer() {
        this(DEFAULT_MAX_VALUE);
    }

    public IntegerRandomizer(int maxValue) {
        Assert.isTrue(maxValue > 0,
                "max value must be positive, actual: " + maxValue);
        this.maxValue = maxValue;
    }

    @Override
    public Integer nextValue() {
        return ThreadLocalRandom.current().nextInt(maxValue);
    }
}
