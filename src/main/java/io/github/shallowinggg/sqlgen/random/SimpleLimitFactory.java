package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.BeanUtils;
import io.github.shallowinggg.sqlgen.util.ClassUtils;
import io.github.shallowinggg.sqlgen.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of the {@link LimitFactory} interface,
 * creating a new {@link Limit} for every request.
 *
 * @author ding shimin
 * @since 1.0
 */
public class SimpleLimitFactory implements LimitFactory {

    private static final Map<Class<?>, Class<? extends Limit<?>>> LIMIT_MAPPING = new ConcurrentHashMap<>();

    static {
        LIMIT_MAPPING.put(Integer.class, IntLimit.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Limit<T> getLimit(Class<T> clazz, T limit) {
        Class<? extends Limit<?>> limitClazz = LIMIT_MAPPING.get(clazz);
        if (limitClazz != null) {
            Constructor<? extends Limit<?>> c = ClassUtils.getConstructorIfAvailable(limitClazz, clazz);
            if (c != null) {
                return (Limit<T>) BeanUtils.instantiateClass(c, limit);
            }
            Method ofMethod = ReflectionUtils.findMethod(limitClazz, "of", clazz);
            if (ofMethod != null) {
                return (Limit<T>) ReflectionUtils.invokeMethod(ofMethod, null, limit);
            }
        }
        return null;
    }
}
