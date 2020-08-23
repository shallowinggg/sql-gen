package io.github.shallowinggg.sqlgen.random;

import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomizer for type {@link Double}. This implementation is
 * simple but bad.
 * <p>
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class BigDecimalRandomizer extends AbstractTypedRandomizer<BigDecimal> {

    private static final double DEFAULT_MAX_VALUE = Double.MAX_VALUE;

    private final double maxValue;

    public static BigDecimalRandomizer create() {
        return new BigDecimalRandomizer();
    }

    public static BigDecimalRandomizer create(double maxValue) {
        return new BigDecimalRandomizer(maxValue);
    }

    public BigDecimalRandomizer() {
        this(DEFAULT_MAX_VALUE);
    }

    public BigDecimalRandomizer(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public BigDecimal nextValue() {
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(maxValue));
    }
}
