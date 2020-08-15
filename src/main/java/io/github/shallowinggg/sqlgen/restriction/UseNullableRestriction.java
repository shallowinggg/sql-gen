package io.github.shallowinggg.sqlgen.restriction;

import io.github.shallowinggg.sqlgen.random.Randomizer;

/**
 * @author ding shimin
 */
class UseNullableRestriction implements ColumnRestriction {

    public static final UseNullableRestriction INSTANCE = new UseNullableRestriction();

    @Override
    public Randomizer<?> randomizer() {
        return null;
    }
}
