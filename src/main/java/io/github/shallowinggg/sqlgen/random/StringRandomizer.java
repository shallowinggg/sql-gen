package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@link Randomizer} implementation for type {@code String}.
 * <p>
 * This implementation only use common character which has
 * no special usage in case of unexpected behaviour.
 * Out of this consideration, '%', '$' etc are all excluded.
 * <p>
 * StringRandomizer can accept a param which means max length
 * of the string that will be generated randomly. If you not
 * specify this value, use {@link #DEFAULT_MAX_LEN} as default.
 * <p>
 * This class is thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class StringRandomizer implements Randomizer<String> {

    private static final int DEFAULT_MAX_LEN = 1000;

    private static final char[] COMMON_TEXT = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            '_', '-'
    };

    private final int maxLen;

    public static StringRandomizer create() {
        return new StringRandomizer();
    }

    public static StringRandomizer create(int maxLen) {
        return new StringRandomizer(maxLen);
    }

    public StringRandomizer() {
        this(DEFAULT_MAX_LEN);
    }

    public StringRandomizer(int maxLen) {
        Assert.isTrue(maxLen > 0, "maxLen must be positive");
        this.maxLen = maxLen;
    }

    @Override
    public String nextValue() {
        final int size = ThreadLocalRandom.current().nextInt(maxLen);
        final int len = COMMON_TEXT.length;
        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < size; ++i) {
            builder.append(COMMON_TEXT[ThreadLocalRandom.current().nextInt(len)]);
        }
        return builder.toString();
    }
}
