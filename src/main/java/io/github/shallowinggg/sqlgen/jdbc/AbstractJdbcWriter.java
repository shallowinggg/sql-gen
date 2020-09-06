package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.JdbcException;
import io.github.shallowinggg.sqlgen.SqlGenException;
import io.github.shallowinggg.sqlgen.jdbc.support.JdbcSupport;
import io.github.shallowinggg.sqlgen.random.AbstractTypedRandomizer;
import io.github.shallowinggg.sqlgen.random.Randomizer;
import io.github.shallowinggg.sqlgen.random.RandomizerFactory;
import io.github.shallowinggg.sqlgen.random.Randomizers;
import io.github.shallowinggg.sqlgen.random.SimpleRandomizerFactory;
import io.github.shallowinggg.sqlgen.util.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ding shimin
 */
public abstract class AbstractJdbcWriter implements JdbcWriter {
    private final Log log = LogFactory.getLog(getClass());

    private final RandomizerFactory randomizerFactory;

    public AbstractJdbcWriter() {
        this(new SimpleRandomizerFactory());
    }

    public AbstractJdbcWriter(RandomizerFactory randomizerFactory) {
        this.randomizerFactory = randomizerFactory;
    }

    @Override
    public void insert(String table, int rows) {
        insert(table, rows, Collections.emptyMap());
    }

    @Override
    public void insert(String table, int rows, Map<String, Randomizer<?>> customColumns) {
        preCheck(table, rows);
        List<BuildInfo> buildInfos = prepareBuildInfos(table, customColumns);

        int affected;
        try {
            affected = doInsert(table, rows, buildInfos);
        } catch (SQLException e) {
            throw new JdbcException("Unexpected error when insert data", e);
        }
        if (log.isInfoEnabled()) {
            log.info("Totally insert " + affected + " rows");
        }
    }

    private void preCheck(String table, int rows) {
        Assert.hasText(table, "table must has text");
        Assert.isTrue(rows > 0, "rows must be positive");

        // TODO: use a interface instead
        DatabaseMetaData databaseMetaData = JdbcSupport.readDatabaseMetaData();
        try {
            if (databaseMetaData.isReadOnly()) {
                throw new SqlGenException("Database is read only");
            }
            if (!JdbcSupport.isTableExist(table)) {
                throw new SqlGenException(String.format("Table [%s] is not exist", table));
            }
        } catch (SQLException e) {
            throw new JdbcException("Unexpected exception when read database metadata", e);
        }
    }

    private List<BuildInfo> prepareBuildInfos(String table, Map<String, Randomizer<?>> customColumns) {
        List<ColumnMetaData> columns = JdbcSupport.readColumnMetaData(table);
        if (log.isDebugEnabled()) {
            log.debug("Column metadata for table [" + table + "]:\n" +
                    columns.stream().map(ColumnMetaData::toString).collect(Collectors.joining("\n")));
        }

        List<BuildInfo> buildInfos = new ArrayList<>(columns.size());
        customColumns = customColumns == null ? Collections.emptyMap() : customColumns;
        for (ColumnMetaData column : columns) {
            String columnName = column.getName();
            Randomizer<?> randomizer = customColumns.get(columnName);
            // skip special column
            if (column.isAutoIncrement() ||
                    (isNullRandomizer(randomizer) && column.isNullable()) ||
                    (isDefaultRandomizer(randomizer) && column.hasDefaultValue())) {
                continue;
            }

            // create default randomizer if non-exist or mismatch
            if (randomizer == null ||
                    (isNullRandomizer(randomizer) && !column.isNullable()) ||
                    (isDefaultRandomizer(randomizer) && !column.hasDefaultValue())) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Create default randomizer for column [%s]", columnName));
                }
                randomizer = createDefaultRandomizer(column);
                if (randomizer == null) {
                    throw new SqlGenException(String.format("Find no suitable randomizer for column [%s]", columnName));
                }
            }
            BuildInfo buildInfo = new BuildInfo(column, randomizer);
            validate(buildInfo);
            buildInfos.add(buildInfo);
        }
        return buildInfos;
    }

    private boolean isNullRandomizer(Randomizer<?> randomizer) {
        return randomizer != null && randomizer == Randomizers.useNull();
    }

    private boolean isDefaultRandomizer(Randomizer<?> randomizer) {
        return randomizer != null && randomizer == Randomizers.useDefault();
    }

    private Randomizer<?> createDefaultRandomizer(ColumnMetaData columnMetaData) {
        return randomizerFactory.getRandomizer(columnMetaData);
    }

    private void validate(BuildInfo buildInfo) {
        Randomizer<?> randomizer = buildInfo.randomizer();
        Class<?> jdbcType = buildInfo.javaType();
        String columnName = buildInfo.columnName();
        if (randomizer instanceof AbstractTypedRandomizer) {
            if (!((AbstractTypedRandomizer<?>) randomizer).supportJdbcType(jdbcType)) {
                throw new IllegalArgumentException(String.format("Illegal randomizer for column [%s], " +
                        "expected column type: [%s]", columnName, jdbcType.getName()));
            }
        } else {
            fallbackCommonValidation(randomizer, jdbcType, columnName);
        }
    }

    private void fallbackCommonValidation(Randomizer<?> randomizer, Class<?> jdbcType, String columnName) {
        Class<?> clazz = randomizer.getClass();
        while (clazz != null) {
            // only check generic interfaces, otherwise you should
            // extend class AbstractTypedRandomizer
            Type[] types = clazz.getGenericInterfaces();
            if (types.length != 0) {
                for (Type type : types) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type rawType = parameterizedType.getRawType();
                    if (rawType instanceof Class<?> && rawType.equals(Randomizer.class)) {
                        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                        if (actualTypeArgument instanceof Class<?> &&
                                jdbcType.isAssignableFrom((Class<?>) actualTypeArgument)) {
                            return;
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalArgumentException(String.format("Illegal randomizer for column [%s], " +
                "expected column type: [%s]", columnName, jdbcType.getName()));
    }

    protected String buildSql(String table, List<BuildInfo> buildInfos) {
        int sz = buildInfos.size();
        StringBuilder columnSql = new StringBuilder(sz * 10);
        StringBuilder placeholderSql = new StringBuilder(sz * 3);
        Iterator<BuildInfo> columnNames = buildInfos.iterator();
        if (columnNames.hasNext()) {
            columnSql.append(columnNames.next().columnName());
            placeholderSql.append("?");
            while (columnNames.hasNext()) {
                columnSql.append(", ").append(columnNames.next().columnName());
                placeholderSql.append(", ?");
            }
        }
        return "INSERT INTO " + table + "(" + columnSql + ") VALUES (" + placeholderSql + ");";
    }


    /**
     * Execute insert operation with prepared infos.
     *
     * @param table      the table to insert
     * @param rows       the amount of data
     * @param buildInfos the base info for insertion
     * @return rows that insert successfully
     * @throws SQLException if a insertion error occurs
     */
    protected abstract int doInsert(String table, int rows, List<BuildInfo> buildInfos) throws SQLException;

    static class BuildInfo {

        private final ColumnMetaData metaData;

        private final Randomizer<?> randomizer;

        BuildInfo(ColumnMetaData metaData, Randomizer<?> randomizer) {
            this.metaData = metaData;
            this.randomizer = randomizer;
        }

        public String columnName() {
            return metaData.getName();
        }

        public int sqlType() {
            return metaData.getSqlType();
        }

        public Object defaultValue() {
            return metaData.getDefaultValue();
        }

        public Class<?> javaType() {
            return metaData.getJavaType();
        }

        public boolean isNullable() {
            return metaData.isNullable();
        }

        public boolean isAutoIncrement() {
            return metaData.isAutoIncrement();
        }

        public boolean hasDefaultValue() {
            return metaData.hasDefaultValue();
        }

        public Randomizer<?> randomizer() {
            return randomizer;
        }
    }
}
