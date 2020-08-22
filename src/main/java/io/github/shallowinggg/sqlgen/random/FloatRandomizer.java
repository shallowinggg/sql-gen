package io.github.shallowinggg.sqlgen.random;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomizer for type {@link Float}.
 * <p>
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class FloatRandomizer extends AbstractTypedRandomizer<Float> {

    private static final float DEFAULT_MAX_VALUE = Float.MAX_VALUE;

    private final float maxValue;

    public static FloatRandomizer create() {
        return new FloatRandomizer();
    }

    public static FloatRandomizer create(float maxValue) {
        return new FloatRandomizer(maxValue);
    }

    public FloatRandomizer() {
        this(DEFAULT_MAX_VALUE);
    }

    public FloatRandomizer(float maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public Float nextValue() {
        return (float) ThreadLocalRandom.current().nextDouble(maxValue);
    }
}
