package io.github.shallowinggg.sqlgen.restriction;

import io.github.shallowinggg.sqlgen.random.Randomizer;

/**
 * @author ding shimin
 */
public class SimpleColumnRestriction implements ColumnRestriction {

    private final Randomizer<?> randomizer;

    public SimpleColumnRestriction(Randomizer<?> randomizer) {
        this.randomizer = randomizer;
    }

    @Override
    public Randomizer<?> randomizer() {
        return randomizer;
    }
}
