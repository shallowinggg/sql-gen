package io.github.shallowinggg.sqlgen.random;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ding shimin
 */
public class BooleanRandomizer implements Randomizer<Boolean> {

    public static BooleanRandomizer create() {
        return new BooleanRandomizer();
    }

    @Override
    public Boolean nextValue() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
