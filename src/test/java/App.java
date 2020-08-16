import io.github.shallowinggg.sqlgen.JdbcException;
import io.github.shallowinggg.sqlgen.config.DbConfig;
import io.github.shallowinggg.sqlgen.jdbc.ColumnMetaData;
import io.github.shallowinggg.sqlgen.jdbc.support.ConnectionFactory;
import io.github.shallowinggg.sqlgen.jdbc.support.JdbcSupport;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static io.github.shallowinggg.sqlgen.jdbc.support.JdbcSupport.readDatabaseMetaData;

/**
 * @author ding shimin
 */
public class App {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/test";
        String driveName = "com.mysql.jdbc.Driver";
        String username = "root";
        String password = "root";

        DbConfig dbConfig = new DbConfig(url, driveName, username, password);
        ConnectionFactory.init(dbConfig);
        List<ColumnMetaData> columns = JdbcSupport.readColumnMetaData("types");
        columns.forEach(System.out::println);

        String sql = "insert into types values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int idx = 1;
            for (ColumnMetaData column : columns) {
                statement.setObject(idx, column.getDefaultValue(), column.getSqlType());
                idx++;
            }
            statement.execute();
        } catch (SQLException e) {
            throw new JdbcException("Unexpected error when insert data", e);
        }
    }

    @Test
    public void test() {
        String url = "jdbc:mysql://localhost:3306/test";
        String driveName = "com.mysql.jdbc.Driver";
        String username = "root";
        String password = "root";

        DbConfig dbConfig = new DbConfig(url, driveName, username, password);
        ConnectionFactory.init(dbConfig);

        DatabaseMetaData databaseMetaData = readDatabaseMetaData();
        try {
            ResultSet indexInfo = databaseMetaData.getIndexInfo(null, null, "idx2", false, false);
            while (indexInfo.next()) {
                String indexName = indexInfo.getString("INDEX_NAME");
                String columnName = indexInfo.getString("COLUMN_NAME");
                //如果为真则说明索引值不唯一，为假则说明索引值必须唯一。
                boolean nonUnique = indexInfo.getBoolean("NON_UNIQUE");
                System.out.println(indexName + "-" + columnName + "-" + nonUnique);
            }

        } catch (SQLException e) {
            throw new JdbcException("Unexpected problem when read column metadata for table ", e);
        }
    }
}
