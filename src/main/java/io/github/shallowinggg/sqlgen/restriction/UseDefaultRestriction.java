package io.github.shallowinggg.sqlgen.restriction;

import io.github.shallowinggg.sqlgen.random.Randomizer;

/**
 * @author ding shimin
 */
class UseDefaultRestriction implements ColumnRestriction {

    public static final UseDefaultRestriction INSTANCE = new UseDefaultRestriction();

    private UseDefaultRestriction() {
    }

    @Override
    public Randomizer<?> randomizer() {
        return null;
    }
}
