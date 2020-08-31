package io.github.shallowinggg.sqlgen.jdbc;

import io.github.shallowinggg.sqlgen.JdbcException;
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
public class BatchJdbcWriter extends JdbcWriter {

    private int batchSize;

    @Override
    protected int insert(String sql, int rows, List<BuildInfo> buildInfos) {
        Connection stub = null;
        try (Connection connection = ConnectionFactory.getInstance().createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            stub = connection;

            BooleanRandomizer bool = BooleanRandomizer.create();
            int count = rows / batchSize + 1;
            int affected = 0;
            for (int i = 0; i < count; ++i) {
                int sz = i == count - 1 ? (rows % batchSize) : batchSize;
                for (int j = 0; j < sz; ++j) {
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
                    statement.addBatch();
                }
                affected += statement.executeBatch().length;
            }
            connection.commit();
            return affected;
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
}
