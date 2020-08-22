package io.github.shallowinggg.sqlgen.random;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Randomizer for type {@link Byte}.
 *
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class ByteRandomizer extends AbstractIntegerRandomizer<Byte> {

    private static final int DEFAULT_MAX_VALUE = Byte.MAX_VALUE + 1;

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
        super(rangeCheck(maxValue, 1, DEFAULT_MAX_VALUE));
    }

    @Override
    public Byte nextValue() {
        return (byte) nextInt();
    }
}
