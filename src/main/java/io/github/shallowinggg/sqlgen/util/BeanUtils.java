package io.github.shallowinggg.sqlgen.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * Static convenience methods for JavaBeans: for instantiating beans,
 * checking bean property types, copying bean properties, etc.
 *
 * <p>Mainly for internal use within the framework, but to some degree also
 * useful for application classes. Consider
 * <a href="https://commons.apache.org/proper/commons-beanutils/">Apache Commons BeanUtils</a>,
 * <a href="https://hotelsdotcom.github.io/bull/">BULL - Bean Utils Light Library</a>,
 * or similar third-party frameworks for more comprehensive bean utilities.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Sebastien Deleuze
 */
public abstract class BeanUtils {

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES;

    static {
        Map<Class<?>, Object> values = new HashMap<>();
        values.put(boolean.class, false);
        values.put(byte.class, (byte) 0);
        values.put(short.class, (short) 0);
        values.put(int.class, 0);
        values.put(long.class, (long) 0);
        DEFAULT_TYPE_VALUES = Collections.unmodifiableMap(values);
    }


    /**
     * Convenience method to instantiate a class using its no-arg constructor.
     *
     * @param clazz class to instantiate
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     * @see Class#newInstance()
     * @deprecated as of Spring 5.0, following the deprecation of
     * {@link Class#newInstance()} in JDK 9
     */
    @Deprecated
    public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new BeanInstantiationException(clazz, "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
        }
    }

    /**
     * Instantiate a class using its 'primary' constructor (for Kotlin classes,
     * potentially having default arguments declared) or its default constructor
     * (for regular Java classes, expecting a standard no-arg setup).
     * <p>Note that this method tries to set the constructor accessible
     * if given a non-accessible (that is, non-public) constructor.
     *
     * @param clazz the class to instantiate
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated.
     *                                    The cause may notably indicate a {@link NoSuchMethodException} if no
     *                                    primary/default constructor was found, a {@link NoClassDefFoundError}
     *                                    or other {@link LinkageError} in case of an unresolvable class definition
     *                                    (e.g. due to a missing dependency at runtime), or an exception thrown
     *                                    from the constructor invocation itself.
     * @see Constructor#newInstance
     */
    public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return instantiateClass(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException ex) {
            throw new BeanInstantiationException(clazz, "No default constructor found", ex);
        } catch (LinkageError err) {
            throw new BeanInstantiationException(clazz, "Unresolvable class definition", err);
        }
    }

    /**
     * Instantiate a class using its no-arg constructor and return the new instance
     * as the specified assignable type.
     * <p>Useful in cases where the type of the class to instantiate (clazz) is not
     * available, but the type desired (assignableTo) is known.
     * <p>Note that this method tries to set the constructor accessible if given a
     * non-accessible (that is, non-public) constructor.
     *
     * @param clazz        class to instantiate
     * @param assignableTo type that clazz must be assignableTo
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     * @see Constructor#newInstance
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo) throws BeanInstantiationException {
        Assert.isAssignable(assignableTo, clazz);
        return (T) instantiateClass(clazz);
    }

    /**
     * Convenience method to instantiate a class using the given constructor.
     * <p>Note that this method tries to set the constructor accessible if given a
     * non-accessible (that is, non-public) constructor, and supports Kotlin classes
     * with optional parameters and default values.
     *
     * @param ctor the constructor to instantiate
     * @param args the constructor arguments to apply (use {@code null} for an unspecified
     *             parameter, Kotlin optional parameters and Java primitive types are supported)
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     * @see Constructor#newInstance
     */
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws BeanInstantiationException {
        Assert.notNull(ctor, "Constructor must not be null");
        try {
            ReflectionUtils.makeAccessible(ctor);
            Class<?>[] parameterTypes = ctor.getParameterTypes();
            Assert.isTrue(args.length <= parameterTypes.length, "Can't specify more arguments than constructor parameters");
            Object[] argsWithDefaultValues = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    Class<?> parameterType = parameterTypes[i];
                    argsWithDefaultValues[i] = (parameterType.isPrimitive() ? DEFAULT_TYPE_VALUES.get(parameterType) : null);
                } else {
                    argsWithDefaultValues[i] = args[i];
                }
            }
            return ctor.newInstance(argsWithDefaultValues);

        } catch (InstantiationException ex) {
            throw new BeanInstantiationException(ctor, "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException(ctor, "Is the constructor accessible?", ex);
        } catch (IllegalArgumentException ex) {
            throw new BeanInstantiationException(ctor, "Illegal arguments for constructor", ex);
        } catch (InvocationTargetException ex) {
            throw new BeanInstantiationException(ctor, "Constructor threw exception", ex.getTargetException());
        }
    }

    /**
     * Find a method with the given method name and the given parameter types,
     * declared on the given class or one of its superclasses. Prefers public methods,
     * but will return a protected, package access, or private method too.
     * <p>Checks {@code Class.getMethod} first, falling back to
     * {@code findDeclaredMethod}. This allows to find public methods
     * without issues even in environments with restricted Java security settings.
     *
     * @param clazz      the class to check
     * @param methodName the name of the method to find
     * @param paramTypes the parameter types of the method to find
     * @return the Method object, or {@code null} if not found
     * @see Class#getMethod
     * @see #findDeclaredMethod
     */
    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return findDeclaredMethod(clazz, methodName, paramTypes);
        }
    }

    /**
     * Find a method with the given method name and the given parameter types,
     * declared on the given class or one of its superclasses. Will return a public,
     * protected, package access, or private method.
     * <p>Checks {@code Class.getDeclaredMethod}, cascading upwards to all superclasses.
     *
     * @param clazz      the class to check
     * @param methodName the name of the method to find
     * @param paramTypes the parameter types of the method to find
     * @return the Method object, or {@code null} if not found
     * @see Class#getDeclaredMethod
     */
    @Nullable
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            if (clazz.getSuperclass() != null) {
                return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
            }
            return null;
        }
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none),
     * declared on the given class or one of its superclasses. Prefers public methods,
     * but will return a protected, package access, or private method too.
     * <p>Checks {@code Class.getMethods} first, falling back to
     * {@code findDeclaredMethodWithMinimalParameters}. This allows for finding public
     * methods without issues even in environments with restricted Java security settings.
     *
     * @param clazz      the class to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     *                                  could not be resolved to a unique method with minimal parameters
     * @see Class#getMethods
     * @see #findDeclaredMethodWithMinimalParameters
     */
    @Nullable
    public static Method findMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = findMethodWithMinimalParameters(clazz.getMethods(), methodName);
        if (targetMethod == null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz, methodName);
        }
        return targetMethod;
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none),
     * declared on the given class or one of its superclasses. Will return a public,
     * protected, package access, or private method.
     * <p>Checks {@code Class.getDeclaredMethods}, cascading upwards to all superclasses.
     *
     * @param clazz      the class to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     *                                  could not be resolved to a unique method with minimal parameters
     * @see Class#getDeclaredMethods
     */
    @Nullable
    public static Method findDeclaredMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = findMethodWithMinimalParameters(clazz.getDeclaredMethods(), methodName);
        if (targetMethod == null && clazz.getSuperclass() != null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
        }
        return targetMethod;
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none)
     * in the given list of methods.
     *
     * @param methods    the methods to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     *                                  could not be resolved to a unique method with minimal parameters
     */
    @Nullable
    public static Method findMethodWithMinimalParameters(Method[] methods, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = null;
        int numMethodsFoundWithCurrentMinimumArgs = 0;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                int numParams = method.getParameterCount();
                if (targetMethod == null || numParams < targetMethod.getParameterCount()) {
                    targetMethod = method;
                    numMethodsFoundWithCurrentMinimumArgs = 1;
                } else if (!method.isBridge() && targetMethod.getParameterCount() == numParams) {
                    if (targetMethod.isBridge()) {
                        // Prefer regular method over bridge...
                        targetMethod = method;
                    } else {
                        // Additional candidate with same length
                        numMethodsFoundWithCurrentMinimumArgs++;
                    }
                }
            }
        }
        if (numMethodsFoundWithCurrentMinimumArgs > 1) {
            throw new IllegalArgumentException("Cannot resolve method '" + methodName +
                    "' to a unique method. Attempted to resolve to overloaded method with " +
                    "the least number of parameters but there were " +
                    numMethodsFoundWithCurrentMinimumArgs + " candidates.");
        }
        return targetMethod;
    }

    /**
     * Parse a method signature in the form {@code methodName[([arg_list])]},
     * where {@code arg_list} is an optional, comma-separated list of fully-qualified
     * type names, and attempts to resolve that signature against the supplied {@code Class}.
     * <p>When not supplying an argument list ({@code methodName}) the method whose name
     * matches and has the least number of parameters will be returned. When supplying an
     * argument type list, only the method whose name and argument types match will be returned.
     * <p>Note then that {@code methodName} and {@code methodName()} are <strong>not</strong>
     * resolved in the same way. The signature {@code methodName} means the method called
     * {@code methodName} with the least number of arguments, whereas {@code methodName()}
     * means the method called {@code methodName} with exactly 0 arguments.
     * <p>If no method can be found, then {@code null} is returned.
     *
     * @param signature the method signature as String representation
     * @param clazz     the class to resolve the method signature against
     * @return the resolved Method
     * @see #findMethod
     * @see #findMethodWithMinimalParameters
     */
    @Nullable
    public static Method resolveSignature(String signature, Class<?> clazz) {
        Assert.hasText(signature, "'signature' must not be empty");
        Assert.notNull(clazz, "Class must not be null");
        int startParen = signature.indexOf('(');
        int endParen = signature.indexOf(')');
        if (startParen > -1 && endParen == -1) {
            throw new IllegalArgumentException("Invalid method signature '" + signature +
                    "': expected closing ')' for args list");
        } else if (startParen == -1 && endParen > -1) {
            throw new IllegalArgumentException("Invalid method signature '" + signature +
                    "': expected opening '(' for args list");
        } else if (startParen == -1) {
            return findMethodWithMinimalParameters(clazz, signature);
        } else {
            String methodName = signature.substring(0, startParen);
            String[] parameterTypeNames =
                    StringUtils.commaDelimitedListToStringArray(signature.substring(startParen + 1, endParen));
            Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
            for (int i = 0; i < parameterTypeNames.length; i++) {
                String parameterTypeName = parameterTypeNames[i].trim();
                try {
                    parameterTypes[i] = ClassUtils.forName(parameterTypeName, clazz.getClassLoader());
                } catch (Throwable ex) {
                    throw new IllegalArgumentException("Invalid method signature: unable to resolve type [" +
                            parameterTypeName + "] for argument " + i + ". Root cause: " + ex);
                }
            }
            return findMethod(clazz, methodName, parameterTypes);
        }
    }


    /**
     * Check if the given type represents a "simple" property: a simple value
     * type or an array of simple value types.
     * <p>See {@link #isSimpleValueType(Class)} for the definition of <em>simple
     * value type</em>.
     * <p>Used to determine properties to check for a "simple" dependency-check.
     *
     * @param type the type to check
     * @return whether the given type represents a "simple" property
     * @see #isSimpleValueType(Class)
     */
    public static boolean isSimpleProperty(Class<?> type) {
        Assert.notNull(type, "'type' must not be null");
        return isSimpleValueType(type) || (type.isArray() && isSimpleValueType(type.getComponentType()));
    }

    /**
     * Check if the given type represents a "simple" value type: a primitive or
     * primitive wrapper, an enum, a String or other CharSequence, a Number, a
     * Date, a Temporal, a URI, a URL, a Locale, or a Class.
     * <p>{@code Void} and {@code void} are not considered simple value types.
     *
     * @param type the type to check
     * @return whether the given type represents a "simple" value type
     * @see #isSimpleProperty(Class)
     */
    public static boolean isSimpleValueType(Class<?> type) {
        return (Void.class != type && void.class != type &&
                (ClassUtils.isPrimitiveOrWrapper(type) ||
                        Enum.class.isAssignableFrom(type) ||
                        CharSequence.class.isAssignableFrom(type) ||
                        Number.class.isAssignableFrom(type) ||
                        Date.class.isAssignableFrom(type) ||
                        Temporal.class.isAssignableFrom(type) ||
                        URI.class == type ||
                        URL.class == type ||
                        Locale.class == type ||
                        Class.class == type));
    }

}

