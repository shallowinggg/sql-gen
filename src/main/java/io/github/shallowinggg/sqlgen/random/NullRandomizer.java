package io.github.shallowinggg.sqlgen.random;

/**
 * @author ding shimin
 */
class NullRandomizer implements Randomizer<Object> {
    static final NullRandomizer INSTANCE = new NullRandomizer();

    private NullRandomizer() {
    }

    @Override
    public Object nextValue() {
        return null;
    }
}
