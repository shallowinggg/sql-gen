package io.github.shallowinggg.sqlgen.random;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomizer for type {@link Double}.
 * <p>
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class DoubleRandomizer extends AbstractTypedRandomizer<Double> {

    private static final double DEFAULT_MAX_VALUE = Double.MAX_VALUE;

    private final double maxValue;

    public static DoubleRandomizer create() {
        return new DoubleRandomizer();
    }

    public static DoubleRandomizer create(double maxValue) {
        return new DoubleRandomizer(maxValue);
    }

    public DoubleRandomizer() {
        this(DEFAULT_MAX_VALUE);
    }

    public DoubleRandomizer(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public Double nextValue() {
        return ThreadLocalRandom.current().nextDouble(maxValue);
    }

}
