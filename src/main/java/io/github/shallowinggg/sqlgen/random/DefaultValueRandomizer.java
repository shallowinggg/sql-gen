package io.github.shallowinggg.sqlgen.random;

/**
 * @author ding shimin
 */
class DefaultValueRandomizer implements Randomizer<Object> {
    static final DefaultValueRandomizer INSTANCE = new DefaultValueRandomizer();

    private DefaultValueRandomizer() {
    }

    @Override
    public Object nextValue() {
        return null;
    }
}
