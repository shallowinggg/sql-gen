package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomizer for type {@link Long}.
 *
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class LongRandomizer extends AbstractTypedRandomizer<Long> {

    private static final long DEFAULT_MAX_VALUE = Long.MAX_VALUE;

    private final long maxValue;

    public static LongRandomizer create() {
        return new LongRandomizer();
    }

    public static LongRandomizer create(int maxValue) {
        return new LongRandomizer(maxValue);
    }

    public LongRandomizer() {
        this(DEFAULT_MAX_VALUE);
    }

    public LongRandomizer(long maxValue) {
        Assert.isTrue(maxValue > 0,
                "max value must be positive, actual: " + maxValue);
        this.maxValue = maxValue;
    }

    @Override
    public Long nextValue() {
        return ThreadLocalRandom.current().nextLong(maxValue);
    }
}
