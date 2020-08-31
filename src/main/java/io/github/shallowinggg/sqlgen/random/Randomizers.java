package io.github.shallowinggg.sqlgen.random;

/**
 * @author ding shimin
 */
public class Randomizers {

    public static Randomizer<?> useNull() {
        return NullRandomizer.INSTANCE;
    }

    public static Randomizer<?> useDefault() {
        return DefaultValueRandomizer.INSTANCE;
    }
}
