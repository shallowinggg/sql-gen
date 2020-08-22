package io.github.shallowinggg.sqlgen.random;

/**
 * Randomizer for type {@code byte[]}.
 *
 * @author ding shimin
 */
public class ByteArrayRandomizer extends AbstractTypedRandomizer<byte[]> {

    @Override
    public byte[] nextValue() {
        return new byte[0];
    }
}
