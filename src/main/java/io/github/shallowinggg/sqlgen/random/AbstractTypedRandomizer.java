package io.github.shallowinggg.sqlgen.random;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * The generic abstract class which implements interface {@link Randomizer}.
 * Any implementation which implements interface {@link Randomizer}
 * <p>
 * This generic abstract class is used for obtaining full generics type information
 * by sub-classing. Any implementation which implements interface {@link Randomizer}
 * should extend this class and this will help check whether this randomizer
 * implementation can produce object that matches the column type of database table
 * ({@link #supportJdbcType(Class)}). It is very important because insert sql with
 * type-mismatch object will produce unexpected {@link java.sql.SQLException}.
 * <p>
 * Class is based on ideas from
 * <a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html"
 * >http://gafter.blogspot.com/2006/12/super-type-tokens.html</a>,
 * Additional idea (from a suggestion made in comments of the article)
 * is to require bogus implementation of <code>Comparable</code>
 * (any such generic interface would do, as long as it forces a method
 * with generic type to be implemented).
 * to ensure that a Type argument is indeed given.
 *
 * Note: sub class shouldn't extend another parameterized class because
 * generic type for this class will be override.
 *
 * @author ding shimin
 * @since 1.0
 */
public abstract class AbstractTypedRandomizer<T> implements Randomizer<T>, Comparable<AbstractTypedRandomizer<T>> {

    protected final Type type;

    protected AbstractTypedRandomizer() {
        Class<?> clazz = getClass();
        while (clazz != null) {
            Type superClass = clazz.getGenericSuperclass();
            if (!(superClass instanceof ParameterizedType)) {
                clazz = clazz.getSuperclass();
            } else {
                break;
            }
        }
        // sanity check, should never happen
        if (clazz == null) {
            throw new IllegalArgumentException("Internal error: AbstractTypedRandomizer constructed without actual type information");
        }
        Type superClass = clazz.getGenericSuperclass();
        /* 22-Dec-2008, tatu: Not sure if this case is safe -- I suspect
         *   it is possible to make it fail?
         *   But let's deal with specific
         *   case when we know an actual use case, and thereby suitable
         *   workarounds for valid case(s) and/or error to throw
         *   on invalid one(s).
         */
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }

    /**
     * Determine if this randomizer can generate the object which satisfies
     * the given jdbc type.
     * <p>
     * This method is mainly used to check whether the object generated by
     * this randomizer matches with the column type. If so, the insertion
     * for table will be successful; otherwise, this may produce unexpected
     * {@link java.sql.SQLException}.
     *
     * @param jdbcType the jdbc type to check
     * @return {@code true} if this randomizer supports the given type
     */
    public final boolean supportJdbcType(Class<?> jdbcType) {
        Objects.requireNonNull(jdbcType, "jdbcType must not be null");
        if (type instanceof Class<?>) {
            return jdbcType.isAssignableFrom((Class<?>) type);
        }
        return false;
    }

    /**
     * The only reason we define this method (and require implementation
     * of <code>Comparable</code>) is to prevent constructing a
     * reference without type information.
     */
    @Override
    public int compareTo(@Nonnull AbstractTypedRandomizer<T> o) {
        return 0;
    }
}
