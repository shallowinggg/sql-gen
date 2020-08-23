package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.jdbc.support.JdbcSupport;
import io.github.shallowinggg.sqlgen.jdbc.support.JdbcTypeJavaClassMappings;
import io.github.shallowinggg.sqlgen.jdbc.support.JdbcTypeNameMapper;
import io.github.shallowinggg.sqlgen.util.Assert;

import javax.annotation.Nullable;

/**
 * @author ding shimin
 */
public class ColumnMetaData {

    private final String name;

    private final int sqlType;

    private final String sqlTypeName;

    private final Class<?> javaType;

    @Nullable
    private final String defaultValue;

    private final int size;

    private final int digits;

    private final boolean nullable;

    private final boolean autoIncrement;

    private final boolean unique;

    public ColumnMetaData(String name, int sqlType, @Nullable String defaultValue, int size, int digits,
                          boolean nullable, boolean autoIncrement, boolean unique) {
        Assert.hasText(name, "name must has text");

        this.name = name;
        this.sqlType = sqlType;
        this.sqlTypeName = JdbcTypeNameMapper.getTypeName(sqlType);
        this.javaType = JdbcTypeJavaClassMappings.INSTANCE.determineJavaClassForJdbcTypeCode(sqlType);
        this.defaultValue = defaultValue;
        this.size = size;
        this.digits = digits;
        this.nullable = nullable;
        this.autoIncrement = autoIncrement;
        this.unique = unique;
    }

    public static ColumnMetaData of(String name, int sqlType, @Nullable String defaultValue, int size, int digits,
                                    boolean nullable, boolean autoIncrement, boolean unique) {
        return new ColumnMetaData(name, sqlType, defaultValue, size, digits, nullable, autoIncrement, unique);
    }

    public int getSqlType() {
        return sqlType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    @Nullable
    public String getDefaultValue() {
        return defaultValue;
    }

    public int getSize() {
        return size;
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
        return JdbcSupport.supportDefaultValue(sqlType) && !"null".equals(defaultValue);
    }

    public boolean isUnique() {
        return unique;
    }

    @Override
    public String toString() {
        return "ColumnMetaData{" +
                "name='" + name + '\'' +
                ", sqlType=" + sqlType +
                ", sqlTypeName='" + sqlTypeName + '\'' +
                ", javaType=" + javaType +
                ", defaultValue='" + defaultValue + '\'' +
                ", size=" + size +
                ", digits=" + digits +
                ", nullable=" + nullable +
                ", autoIncrement=" + autoIncrement +
                ", unique=" + unique +
                '}';
    }
}
