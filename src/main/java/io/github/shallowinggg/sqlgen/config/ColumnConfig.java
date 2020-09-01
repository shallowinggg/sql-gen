package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.random.Randomizer;
import io.github.shallowinggg.sqlgen.restriction.ColumnRestriction;

/**
 * @author ding shimin
 */
public class ColumnConfig {

    private String name;

    private Randomizer<?> randomizer;

    private ColumnRestriction restriction;

    public ColumnConfig(String name, Randomizer<?> randomizer) {
        this.name = name;
        this.randomizer = randomizer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Randomizer<?> getRandomizer() {
        return randomizer;
    }

    public void setRandomizer(Randomizer<?> randomizer) {
        this.randomizer = randomizer;
    }

    public ColumnRestriction getRestriction() {
        return restriction;
    }

    public void setRestriction(ColumnRestriction restriction) {
        this.restriction = restriction;
    }
}
