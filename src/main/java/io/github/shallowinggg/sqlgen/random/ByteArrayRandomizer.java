package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomizer for type {@code byte[]}.
 * <p>
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class ByteArrayRandomizer extends AbstractTypedRandomizer<byte[]> {

    private static final int DEFAULT_MAX_SIZE = 1000;

    private final int maxSize;

    public ByteArrayRandomizer() {
        this(DEFAULT_MAX_SIZE);
    }

    public ByteArrayRandomizer(int maxSize) {
        Assert.isTrue(maxSize > 0, "maxSize must be positive, actual: " + maxSize);
        this.maxSize = maxSize;
    }

    @Override
    public byte[] nextValue() {
        int size = ThreadLocalRandom.current().nextInt(maxSize);
        if (size == 0) {
            return new byte[0];
        } else {
            byte[] bytes = new byte[size];
            ThreadLocalRandom.current().nextBytes(bytes);
            return bytes;
        }
    }
}
