package io.github.shallowinggg.sqlgen.random;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomizer for type {@link Boolean}.
 *
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class BooleanRandomizer extends AbstractTypedRandomizer<Boolean> {

    public static BooleanRandomizer create() {
        return new BooleanRandomizer();
    }

    @Override
    public Boolean nextValue() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
