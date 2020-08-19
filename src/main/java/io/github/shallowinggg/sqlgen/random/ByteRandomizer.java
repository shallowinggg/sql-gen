package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ding shimin
 */
@ThreadSafe
public class ByteRandomizer implements Randomizer<Byte> {

    private static final int DEFAULT_MAX_VALUE = Byte.MAX_VALUE + 1;

    private final byte maxValue;

    public static ByteRandomizer create() {
        return new ByteRandomizer();
    }

    public static ByteRandomizer create(int maxValue) {
        return new ByteRandomizer(maxValue);
    }

    public ByteRandomizer() {
        this(DEFAULT_MAX_VALUE);
    }

    public ByteRandomizer(int maxValue) {
        Assert.isTrue(maxValue > 0 && maxValue <= DEFAULT_MAX_VALUE,
                "max value must not out of range, expected: (0, 128], actual: " + maxValue);
        this.maxValue = (byte) maxValue;
    }

    @Override
    public Byte nextValue() {
        return (byte) ThreadLocalRandom.current().nextInt(maxValue);
    }
}
