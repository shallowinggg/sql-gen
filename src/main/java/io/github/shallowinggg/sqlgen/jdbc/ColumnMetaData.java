package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.util.Assert;

/**
 * @author ding shimin
 */
public class ColumnMetaData {

    private final String name;
    private final int sqlType;
    private final String type;
    private final String defaultValue;
    private final int size;
    private final boolean nullable;
    private final boolean autoIncrement;

    public ColumnMetaData(String name, int sqlType, String type, String defaultValue, int size, boolean nullable, boolean autoIncrement) {
        Assert.hasText(name, "name must has text");
        Assert.hasText(type, "type must has text");

        this.name = name;
        this.sqlType = sqlType;
        this.type = type;
        this.defaultValue = defaultValue;
        this.size = size;
        this.nullable = nullable;
        this.autoIncrement = autoIncrement;
    }

    public static ColumnMetaData of(String name, int sqlType, String type, String defaultValue, int size, boolean nullable,
                                    boolean autoIncrement) {
        return new ColumnMetaData(name, sqlType, type, defaultValue, size, nullable, autoIncrement);
    }

    public int getSqlType() {
        return sqlType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean hasDefaultValue() {
        return !"null".equals(defaultValue);
    }

    @Override
    public String toString() {
        return "ColumnMetaData{" +
                "name='" + name + '\'' +
                ", sqlType=" + sqlType +
                ", type='" + type + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", size=" + size +
                ", nullable=" + nullable +
                ", autoIncrement=" + autoIncrement +
                '}';
    }
}
