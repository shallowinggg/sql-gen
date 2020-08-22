package io.github.shallowinggg.sqlgen.random;

/**
 * Randomizer for type {@link Short}.
 *
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
public class ShortRandomizer extends AbstractIntegerRandomizer<Short> {

    private static final int DEFAULT_MAX_VALUE = Short.MAX_VALUE + 1;

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
        super(rangeCheck(maxValue, 1, DEFAULT_MAX_VALUE));
    }

    @Override
    public Short nextValue() {
        return (short) nextInt();
    }
}
