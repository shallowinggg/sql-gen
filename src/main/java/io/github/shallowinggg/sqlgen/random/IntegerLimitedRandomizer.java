package io.github.shallowinggg.sqlgen.random;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ding shimin
 */
public class IntegerLimitedRandomizer implements LimitedRandomizer<Integer, Integer> {

    @Override
    public Integer nextValue(Limit<Integer> limit) {
        return ThreadLocalRandom.current().nextInt(limit.getLimit());
    }

    @Override
    public Integer nextValue() {
        return ThreadLocalRandom.current().nextInt();
    }
}
