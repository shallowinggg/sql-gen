package io.github.shallowinggg.sqlgen.jdbc.support;

import io.github.shallowinggg.sqlgen.util.Assert;

/**
 * @author ding shimin
 */
public enum ColumnAutoIncrement {

    /**
     * if the column is auto incremented
     */
    TRUE(true, "YES"),
    /**
     * if the column is not auto incremented
     */
    FALSE(false, "NO"),
    /**
     * if it cannot be determined whether the column is auto incremented
     */
    UNKNOWN(false, "empty string");

    private final boolean value;
    private final String description;

    ColumnAutoIncrement(boolean value, String description) {
        Assert.hasText(description, "description must has text");
        this.value = value;
        this.description = description;
    }

    public static boolean isAutoIncrement(String description) {
        for (ColumnAutoIncrement type : values()) {
            if (type.description.equals(description)) {
                return type.value;
            }
        }
        return false;
    }
}
