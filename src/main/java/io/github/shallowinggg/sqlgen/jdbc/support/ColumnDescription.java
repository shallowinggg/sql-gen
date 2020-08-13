package io.github.shallowinggg.sqlgen.jdbc.support;

/**
 * Description of table columns.
 *
 * @author ding shimin
 * @see java.sql.DatabaseMetaData#getColumns(String, String, String, String)
 */
public final class ColumnDescription {

    /**
     * column name
     */
    public static final String COLUMN_NAME = "COLUMN_NAME";

    /**
     * SQL type from {@link java.sql.Types}
     */
    public static final String COLUMN_TYPE = "DATA_TYPE";

    /**
     * column size
     */
    public static final String COLUMN_SIZE = "COLUMN_SIZE";

    /**
     * default value for the column, which should be interpreted as a string when
     * the value is enclosed in single quotes (may be <code>null</code>)
     */
    public static final String COLUMN_DEFAULT = "COLUMN_DEF";

    /**
     * is NULL allowed.
     */
    public static final String COLUMN_NULLABLE = "NULLABLE";

    /**
     * might not allow <code>NULL</code> values
     */
    public static final String COLUMN_NO_NULL = "columnNoNulls";

    /**
     * definitely allows <code>NULL</code> values
     */
    public static final String COLUMN_NULL = "columnNullable";

    /**
     * columnNullableUnknown - nullability unknown
     */
    public static final String COLUMN_NULL_UNKNOWN = "columnNullableUnknown";

    /**
     * Indicates whether this column is auto incremented
     */
    public static final String COLUMN_AUTOINCREMENT = "IS_AUTOINCREMENT";

    private ColumnDescription() {}
}
