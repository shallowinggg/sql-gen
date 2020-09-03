package io.github.shallowinggg.sqlgen.random;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ding shimin
 */
public class AbstractTypedRandomizerTest {

    @Test
    public void directExtendTypedRandomizer() {
        StringTypeRandomizer randomizer = new StringTypeRandomizer();
        Assert.assertTrue(randomizer.supportJdbcType(String.class));
        Assert.assertEquals(String.class, randomizer.getType());
    }

    @Test
    public void anonymousSubClass() {
        Assert.assertTrue(new AbstractTypedRandomizer<String>() {
            @Override
            public String nextValue() {
                return null;
            }
        }.supportJdbcType(String.class));
    }

    @Test
    public void multiLevelInheritanceSubClass() {
        MultiLevelInheritanceRandomizer randomizer = new MultiLevelInheritanceRandomizer();
        Assert.assertTrue(randomizer.supportJdbcType(String.class));
    }

    @Test
    public void nonDirectGenericSubClass() {
        NonDirectGenericRandomizer randomizer = new NonDirectGenericRandomizer();
        Assert.assertTrue(randomizer.supportJdbcType(String.class));
    }

    @Test
    public void multiTypeVariable() {
        Assert.assertTrue(new MultiTypeVariable().supportJdbcType(String.class));

        // This is a bad and error implementation but can't be detected
        Assert.assertFalse(new MultiTypeVariable2().supportJdbcType(Integer.class));
    }

    private static class StringTypeRandomizer extends AbstractTypedRandomizer<String> {
        @Override
        public String nextValue() {
            return null;
        }
    }

    private static class DirectRandomizer<T> extends AbstractTypedRandomizer<T> {
        @Override
        public T nextValue() {
            return null;
        }
    }

    private static class MultiLevelInheritanceRandomizer extends DirectRandomizer<String> {
    }

    private static class NonDirectGenericRandomizer extends MultiLevelInheritanceRandomizer {
    }

    private abstract static class MultiTypeVariableRandomizer<T, U> extends AbstractTypedRandomizer<T> {
    }

    private static class MultiTypeVariable extends MultiTypeVariableRandomizer<String, Integer> {
        @Override
        public String nextValue() {
            return null;
        }
    }

    /**
     * Bad implementation for {@link AbstractTypedRandomizer}, the randomizer type
     * will be detected as {@link U} instead of {@link T}.
     *
     * @param <U> custom type
     * @param <T> randomizer type
     */
    private abstract static class MultiTypeVariableRandomizer2<U, T> extends AbstractTypedRandomizer<T> {
    }

    private static class MultiTypeVariable2 extends MultiTypeVariableRandomizer2<String, Integer> {
        @Override
        public Integer nextValue() {
            return null;
        }
    }
}
