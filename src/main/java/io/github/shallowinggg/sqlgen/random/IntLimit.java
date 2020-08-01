package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

/**
 * Integer implementation for interface {@link Limit}.
 *
 * @author ding shimin
 * @since 1.0
 */
public class IntLimit implements Limit<Integer> {

    private final Integer limit;

    public IntLimit(Integer limit) {
        Assert.notNull(limit, "limit must not be null");
        this.limit = limit;
    }

    public static IntLimit of(Integer limit) {
        return new IntLimit(limit);
    }

    @Override
    public Integer getLimit() {
        return limit;
    }
}
