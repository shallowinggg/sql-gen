package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.restriction.ColumnRestriction;

/**
 * @author ding shimin
 */
public class ColumnConfig {

    private String name;

    private ColumnRestriction restriction;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnRestriction getRestriction() {
        return restriction;
    }

    public void setRestriction(ColumnRestriction restriction) {
        this.restriction = restriction;
    }
}
