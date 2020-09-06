package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory;
import io.github.shallowinggg.sqlgen.random.BooleanRandomizer;
import io.github.shallowinggg.sqlgen.random.Randomizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author ding shimin
 */
public class SimpleJdbcWriter extends AbstractJdbcWriter {

    @Override
    protected int doInsert(String table, int rows, List<BuildInfo> buildInfos) throws SQLException {
        String sql = buildSql(table, buildInfos);
        try (Connection connection = ConnectionFactory.getInstance().createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            BooleanRandomizer bool = BooleanRandomizer.create();
            int affected = 0;
            for (int i = 0; i < rows; ++i) {
                int idx = 1;
                for (BuildInfo buildInfo : buildInfos) {
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
                statement.execute();
                ++affected;
            }
            return affected;
        }
    }
}
