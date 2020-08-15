package io.github.shallowinggg.sqlgen.restriction;

/**
 * @author ding shimin
 */
public class ColumnRestrictions {

    public static ColumnRestriction useNullable() {
        return UseNullableRestriction.INSTANCE;
    }

    public static ColumnRestriction useDefault() {
        return UseDefaultRestriction.INSTANCE;
    }
}
