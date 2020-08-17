package io.github.shallowinggg.sqlgen.restriction;

import io.github.shallowinggg.sqlgen.random.Randomizer;

/**
 * @author ding shimin
 */
public interface ColumnRestriction {

    /**
     * @return
     */
    <T> Randomizer<T> randomizer();
}
