package io.github.shallowinggg.sqlgen.jdbc.support;

import java.sql.DatabaseMetaData;

/**
 * @author ding shimin
 */
public enum ColumnNullable {

    /**
     * allow NULL
     */
    TRUE(true, DatabaseMetaData.columnNullable),

    /**
     * not allow NULL
     */
    FALSE(false, DatabaseMetaData.columnNoNulls),

    /**
     * unknown
     */
    UNKNOWN(false, DatabaseMetaData.columnNullableUnknown);

    private final boolean value;

    private final int code;

    ColumnNullable(boolean isNullable, int code) {
        this.value = isNullable;
        this.code = code;
    }

    public static boolean isNullable(int code) {
        for (ColumnNullable type : values()) {
            if (type.code == code) {
                return type.value;
            }
        }
        return false;
    }
}
