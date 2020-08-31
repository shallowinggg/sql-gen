package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.JdbcException;
import io.github.shallowinggg.sqlgen.SqlGenException;
import io.github.shallowinggg.sqlgen.config.ColumnConfig;
import io.github.shallowinggg.sqlgen.jdbc.support.JdbcSupport;
import io.github.shallowinggg.sqlgen.random.AbstractTypedRandomizer;
import io.github.shallowinggg.sqlgen.random.Randomizer;
import io.github.shallowinggg.sqlgen.random.RandomizerFactory;
import io.github.shallowinggg.sqlgen.random.Randomizers;
import io.github.shallowinggg.sqlgen.random.SimpleRandomizerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ding shimin
 */
public abstract class JdbcWriter {
    private final Log log = LogFactory.getLog(getClass());

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final int batchSize;

    private final RandomizerFactory randomizerFactory;

    public JdbcWriter() {
        this(new SimpleRandomizerFactory(), DEFAULT_BATCH_SIZE);
    }

    public JdbcWriter(int batchSize) {
        this(new SimpleRandomizerFactory(), batchSize);
    }

    public JdbcWriter(RandomizerFactory randomizerFactory, int batchSize) {
        this.randomizerFactory = randomizerFactory;
        this.batchSize = batchSize;
    }

    public void execute(String tableName, int rows, Map<String, ColumnConfig> columnConfigs) {
        DatabaseMetaData databaseMetaData = JdbcSupport.readDatabaseMetaData();
        try {
            if (databaseMetaData.isReadOnly()) {
                throw new SqlGenException("Database is read only");
            }
        } catch (SQLException e) {
            throw new JdbcException("Unexpected exception when read database metadata", e);
        }

        List<ColumnMetaData> columnMetaDataList = JdbcSupport.readColumnMetaData(tableName);
        if (log.isDebugEnabled()) {
            StringBuilder metadataBuilder = new StringBuilder(columnMetaDataList.size() * 30);
            columnMetaDataList.forEach(columnMetaData -> metadataBuilder.append(columnMetaData.toString()).append("\n"));
            log.debug("Column metadata for table [" + tableName + "]:\n" + metadataBuilder.toString());
        }

        List<BuildInfo> buildInfos = new ArrayList<>(columnMetaDataList.size());
        for (ColumnMetaData column : columnMetaDataList) {
            Randomizer<?> randomizer = columnConfigs.get(column.getName()).getRandomizer();
            if (randomizer == null) {
                randomizer = createDefaultRandomizer(column);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Create default randomizer for column [%s]", column.getName()));
                }
            }
            BuildInfo buildInfo = new BuildInfo(column, randomizer);
            validate(buildInfo);
            buildInfos.add(buildInfo);
        }
        execute0(tableName, rows, buildInfos);
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

    private void execute0(String tableName, int rows, List<BuildInfo> buildInfos) {
        Iterator<BuildInfo> itr = buildInfos.iterator();
        while (itr.hasNext()) {
            BuildInfo buildInfo = itr.next();
            if (buildInfo.isAutoIncrement()) {
                itr.remove();
            }
            if (buildInfo.isNullable() && Randomizers.useNull() == buildInfo.randomizer()) {
                itr.remove();
            }
            if (buildInfo.hasDefaultValue() && Randomizers.useDefault() == buildInfo.randomizer()) {
                itr.remove();
            }
        }

        StringBuilder columnSql = new StringBuilder();
        StringBuilder placeholderSql = new StringBuilder();
        Iterator<BuildInfo> columnNames = buildInfos.iterator();
        if (columnNames.hasNext()) {
            columnSql.append(columnNames.next().columnName());
            placeholderSql.append("?");
            while (columnNames.hasNext()) {
                columnSql.append(", ").append(columnNames.next().columnName());
                placeholderSql.append(", ?");
            }
        }
        String sql = "INSERT INTO " + tableName + "(" + columnSql + ") VALUES (" + placeholderSql + ");";
        if (log.isInfoEnabled()) {
            log.info("Auto generated sql: " + sql);
        }

        int affected = 0;
        try {
            affected = insert(sql, rows, buildInfos);
        } catch (SQLException e) {
            throw new JdbcException("Unexpected error when insert data", e);
        }
        if (log.isInfoEnabled()) {
            log.info("Totally insert " + affected + " rows");
        }

    }

    protected abstract int insert(String sql, int rows, List<BuildInfo> buildInfos) throws SQLException;

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
