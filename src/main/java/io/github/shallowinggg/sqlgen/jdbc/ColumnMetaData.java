package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.util.Assert;

/**
 * @author ding shimin
 */
public class ColumnMetaData {

    private final String name;
    private final String type;
    private final String defaultValue;
    private final int size;
    private final boolean nullable;
    private final boolean autoIncrement;

    public ColumnMetaData(String name, String type) {
        this(name, type, null, -1);
    }

    public ColumnMetaData(String name, String type, String defaultValue, int size) {
        this(name, type, defaultValue, size, false, false);
    }

    public ColumnMetaData(String name, String type, String defaultValue, int size, boolean nullable, boolean autoIncrement) {
        Assert.hasText(name, "name must has text");
        Assert.hasText(type, "type must has text");

        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.size = size;
        this.nullable = nullable;
        this.autoIncrement = autoIncrement;
    }

    public static ColumnMetaData make(String name, String type) {
        return new ColumnMetaData(name, type);
    }

    public static ColumnMetaData make(String name, String type, String defaultValue, int size) {
        return new ColumnMetaData(name, type, defaultValue, size);
    }

    public static ColumnMetaData make(String name, String type, String defaultValue, int size, boolean nullable,
                                      boolean autoIncrement) {
        return new ColumnMetaData(name, type, defaultValue, size, nullable, autoIncrement);
    }



    public boolean isNullable() {
        return nullable;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }
}
