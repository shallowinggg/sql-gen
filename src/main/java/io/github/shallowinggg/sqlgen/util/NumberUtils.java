package io.github.shallowinggg.sqlgen.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ding shimin
 */
public abstract class NumberUtils {

    private static final Map<Integer, Integer> INT_10X = new HashMap<>();
    private static final Map<Integer, Long> LONG_10X = new HashMap<>();

    static {
        INT_10X.put(1, 10);
        INT_10X.put(2, 100);
        INT_10X.put(3, 1000);
        INT_10X.put(4, 10000);
        INT_10X.put(5, 100000);
        INT_10X.put(6, 1000000);
        INT_10X.put(7, 10000000);
        INT_10X.put(8, 100000000);
        INT_10X.put(9, 1000000000);
        INT_10X.put(10, Integer.MAX_VALUE);

        LONG_10X.put(1, 10L);
        LONG_10X.put(2, 100L);
        LONG_10X.put(3, 1000L);
        LONG_10X.put(4, 10000L);
        LONG_10X.put(5, 100000L);
        LONG_10X.put(6, 1000000L);
        LONG_10X.put(7, 10000000L);
        LONG_10X.put(8, 100000000L);
        LONG_10X.put(9, 1000000000L);
        LONG_10X.put(10, 10000000000L);
        LONG_10X.put(11, 100000000000L);
        LONG_10X.put(12, 1000000000000L);
        LONG_10X.put(13, 10000000000000L);
        LONG_10X.put(14, 100000000000000L);
        LONG_10X.put(15, 1000000000000000L);
        LONG_10X.put(16, 10000000000000000L);
        LONG_10X.put(17, 100000000000000000L);
        LONG_10X.put(18, 1000000000000000000L);
        LONG_10X.put(19, Long.MAX_VALUE);
    }

    public static Integer max10xInt(int digits) {
        Assert.isTrue(digits < 11, "digits must not be negative than 11");
        return INT_10X.get(digits);
    }

    public static Long max10xLong(int digits) {
        Assert.isTrue(digits < 20, "digits must not be negative than 20");
        return LONG_10X.get(digits);
    }
}
