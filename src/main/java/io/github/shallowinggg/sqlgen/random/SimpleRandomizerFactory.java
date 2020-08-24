package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.jdbc.ColumnMetaData;
import io.github.shallowinggg.sqlgen.util.Assert;
import io.github.shallowinggg.sqlgen.util.BeanUtils;
import io.github.shallowinggg.sqlgen.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ding shimin
 */
public class SimpleRandomizerFactory implements RandomizerFactory {

    private static final Map<Class<?>, Class<? extends Randomizer<?>>> RANDOMIZER_MAP;

    static {
        RANDOMIZER_MAP = new HashMap<>(32);
        RANDOMIZER_MAP.put(Boolean.class, BooleanRandomizer.class);
        RANDOMIZER_MAP.put(Byte.class, ByteRandomizer.class);
        RANDOMIZER_MAP.put(Short.class, ShortRandomizer.class);
        RANDOMIZER_MAP.put(Integer.class, IntegerRandomizer.class);
        RANDOMIZER_MAP.put(Long.class, LongRandomizer.class);
        RANDOMIZER_MAP.put(Float.class, FloatRandomizer.class);
        RANDOMIZER_MAP.put(Double.class, DoubleRandomizer.class);
        RANDOMIZER_MAP.put(BigDecimal.class, BigDecimalRandomizer.class);
        RANDOMIZER_MAP.put(String.class, StringRandomizer.class);
        RANDOMIZER_MAP.put(byte[].class, ByteArrayRandomizer.class);
        RANDOMIZER_MAP.put(java.sql.Date.class, DateRandomizer.class);
        RANDOMIZER_MAP.put(java.sql.Time.class, TimeRandomizer.class);
        RANDOMIZER_MAP.put(java.sql.Timestamp.class, TimestampRandomizer.class);
    }

    public static void registerDefaultRandomizer(Class<?> jdbcType, Class<? extends Randomizer<?>> randomizerType) {
        Assert.notNull(jdbcType, "jdbcType must not be null");
        Assert.notNull(randomizerType, "randomizerType must not be null");
        RANDOMIZER_MAP.put(jdbcType, randomizerType);
    }

    @Nullable
    public static Class<? extends Randomizer<?>> getDefaultRandomizer(Class<?> jdbcType) {
        Assert.notNull(jdbcType, "jdbcType must not be null");
        return RANDOMIZER_MAP.get(jdbcType);
    }

    @Override
    public Randomizer<?> getRandomizer(ColumnMetaData column) {
        Class<?> columnType = column.getJavaType();
        Class<? extends Randomizer<?>> randomizer = RANDOMIZER_MAP.get(columnType);
        if (randomizer == null) {
            throw new IllegalArgumentException("Find no randomizer for column type: " + columnType);
        }

        if (String.class.equals(columnType)) {
            int size = column.getSize();
            try {
                Constructor<? extends Randomizer<?>> c = ReflectionUtils.accessibleConstructor(randomizer, int.class);
                return BeanUtils.instantiateClass(c, size);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(String.format("No available constructor for column: [%s], " +
                        "type: [%s], randomizer: [%s]", column.getName(), columnType, randomizer), e);
            }
        }
        return BeanUtils.instantiateClass(randomizer);
    }
}
