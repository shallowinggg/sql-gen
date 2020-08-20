package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.JdbcException;
import io.github.shallowinggg.sqlgen.SqlGenException;
import io.github.shallowinggg.sqlgen.config.ColumnConfig;
import io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory;
import io.github.shallowinggg.sqlgen.jdbc.support.JdbcSupport;
import io.github.shallowinggg.sqlgen.random.BooleanRandomizer;
import io.github.shallowinggg.sqlgen.random.Randomizer;
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
                Randomizer<?> randomizer = createDefaultRandomizer(column.getJavaType(), column.getSize());
                restriction = new SimpleColumnRestriction(randomizer);
            }
            BuildInfo buildInfo = new BuildInfo(column, restriction);
            validate(buildInfo);
            buildInfoMap.put(column.getName(), buildInfo);
        }
        build(buildInfoMap);
    }

    private <T> Randomizer<T> createDefaultRandomizer(Class<T> type, int size) {
        return null;
    }

    private void validate(BuildInfo buildInfo) {
        Randomizer<?> randomizer = buildInfo.randomizer();
        Class<?> randomType = findParameterType(randomizer);
        Class<?> requireType = buildInfo.javaType();
        if (randomType == null || !requireType.isAssignableFrom(randomType)) {
            throw new RuntimeException(String.format("Illegal restriction for column [%s], " +
                            "expected type: [%s], restriction type: [%s]", buildInfo.columnName(),
                    requireType, randomType));
        }
    }

    static Class<?> findParameterType(Randomizer<?> randomizer) {
        Class<?> type = randomizer.getClass();
        while (type != null) {
            Type[] types = type.getGenericInterfaces();
            if (types.length == 0) {
                type = type.getSuperclass();
            } else {
                ParameterizedType parameterizedType = (ParameterizedType) types[0];
                Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                if(actualTypeArgument instanceof Class<?>) {
                    return (Class<?>) actualTypeArgument;
                }
            }
        }
        return null;
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
        String sql = "INSERT INTO " + tableName + "(" + columnSql + ")\n" +
                "VALUES (" + placeholderSql + ");";
        log.info("Auto generate sql: " + sql);

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
            int affect = statement.executeBatch().length;
            log.info("Totally insert " + affect + " rows");
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
