package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.random.Randomizer;

/**
 * @author ding shimin
 */
public class ColumnConfig {

    private String name;

    private Randomizer<?> randomizer;

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

}
