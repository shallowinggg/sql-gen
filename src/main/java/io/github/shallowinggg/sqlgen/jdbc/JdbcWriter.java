package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.JdbcException;
import io.github.shallowinggg.sqlgen.SqlGenException;
import io.github.shallowinggg.sqlgen.config.ColumnConfig;
import io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory;
import io.github.shallowinggg.sqlgen.jdbc.support.JdbcSupport;
import io.github.shallowinggg.sqlgen.random.AbstractTypedRandomizer;
import io.github.shallowinggg.sqlgen.random.BooleanRandomizer;
import io.github.shallowinggg.sqlgen.random.Randomizer;
import io.github.shallowinggg.sqlgen.random.RandomizerFactory;
import io.github.shallowinggg.sqlgen.random.SimpleRandomizerFactory;
import io.github.shallowinggg.sqlgen.restriction.ColumnRestriction;
import io.github.shallowinggg.sqlgen.restriction.ColumnRestrictions;
import io.github.shallowinggg.sqlgen.restriction.SimpleColumnRestriction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ding shimin
 */
public class JdbcWriter {
    private final Log log = LogFactory.getLog(getClass());

    private String tableName;

    private int rows;

    private RandomizerFactory randomizerFactory = new SimpleRandomizerFactory();

    private Map<String, ColumnConfig> columnConfigMap;

    public JdbcWriter(Map<String, ColumnConfig> columnConfigMap) {
        this.columnConfigMap = columnConfigMap;
    }

    public void execute(String tableName) throws SQLException {
        DatabaseMetaData databaseMetaData = JdbcSupport.readDatabaseMetaData();
        if (databaseMetaData.isReadOnly()) {
            throw new SqlGenException("Database is read only");
        }

        List<ColumnMetaData> columnMetaDataList = JdbcSupport.readColumnMetaData(tableName);
        if (log.isDebugEnabled()) {
            StringBuilder metadataBuilder = new StringBuilder(columnMetaDataList.size() * 30);
            columnMetaDataList.forEach(columnMetaData -> metadataBuilder.append(columnMetaData.toString()));
            log.debug("Column metadata for table [" + tableName + "]:\n" + metadataBuilder.toString());
        }

        LinkedHashMap<String, BuildInfo> buildInfoMap = new LinkedHashMap<>(columnMetaDataList.size());
        for (ColumnMetaData column : columnMetaDataList) {
            ColumnRestriction restriction = columnConfigMap.get(column.getName()).getRestriction();
            if (restriction == null) {
                Randomizer<?> randomizer = createDefaultRandomizer(column);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Create default randomizer for column [%s]", column.getName()));
                }
                restriction = new SimpleColumnRestriction(randomizer);
            }
            BuildInfo buildInfo = new BuildInfo(column, restriction);
            validate(buildInfo);
            buildInfoMap.put(column.getName(), buildInfo);
        }
        build(buildInfoMap);
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

    private void build(LinkedHashMap<String, BuildInfo> map) {
        Iterator<Map.Entry<String, BuildInfo>> itr = map.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, BuildInfo> entry = itr.next();
            BuildInfo buildInfo = entry.getValue();
            if (buildInfo.isAutoIncrement()) {
                itr.remove();
            }
            if (buildInfo.isNullable() && ColumnRestrictions.useNullable() == buildInfo.restriction) {
                itr.remove();
            }
            if (buildInfo.hasDefaultValue() && ColumnRestrictions.useDefault() == buildInfo.restriction) {
                itr.remove();
            }
        }

        StringBuilder columnSql = new StringBuilder();
        StringBuilder placeholderSql = new StringBuilder();
        map.keySet().forEach(columnName -> {
            columnSql.append(columnName).append(", ");
            placeholderSql.append("?, ");
        });
        columnSql.setLength(columnSql.length() - 2);
        placeholderSql.setLength(placeholderSql.length() - 2);
        String sql = "INSERT INTO " + tableName + "(" + columnSql + ") VALUES (" + placeholderSql + ");";
        if (log.isInfoEnabled()) {
            log.info("Auto generate sql: " + sql);
        }

        Connection stub = null;
        try (Connection connection = ConnectionFactory.getInstance().createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            stub = connection;

            BooleanRandomizer bool = BooleanRandomizer.create();
            for (int i = 0; i < rows; ++i) {
                int idx = 1;
                for (BuildInfo buildInfo : map.values()) {
                    Randomizer<?> randomizer = buildInfo.randomizer();
                    Object val;
                    if (buildInfo.isNullable() && Boolean.TRUE.equals(bool.nextValue())) {
                        statement.setNull(idx, buildInfo.sqlType());
                        ++idx;
                        continue;
                    }
                    if (buildInfo.hasDefaultValue() && Boolean.TRUE.equals(bool.nextValue())) {
                        statement.setObject(idx, buildInfo.defaultValue());
                        ++idx;
                        continue;
                    }
                    val = randomizer.nextValue();
                    statement.setObject(idx, val);
                    ++idx;
                }
                statement.addBatch();
            }
            int affected = statement.executeBatch().length;
            if (log.isInfoEnabled()) {
                log.info("Totally insert " + affected + " rows");
            }
        } catch (SQLException e) {
            if (stub != null) {
                try {
                    stub.rollback();
                } catch (SQLException e2) {
                    throw new JdbcException("Unexpected error when insert data", e2);
                }
            }
            throw new JdbcException("Unexpected error when insert data", e);
        }

    }

    private static class BuildInfo {
        private final ColumnMetaData metaData;

        private final ColumnRestriction restriction;

        BuildInfo(ColumnMetaData metaData, ColumnRestriction restriction) {
            this.metaData = metaData;
            this.restriction = restriction;
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
            return restriction.randomizer();
        }
    }
}
