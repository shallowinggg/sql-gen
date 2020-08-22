package io.github.shallowinggg.sqlgen.random;

/**
 * Randomizer for type {@link Integer}.
 *
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
public class IntegerRandomizer extends AbstractIntegerRandomizer<Integer> {

    private static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;

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
        super(rangeCheck(maxValue, 1, DEFAULT_MAX_VALUE));
    }

    @Override
    public Integer nextValue() {
        return nextInt();
    }
}
