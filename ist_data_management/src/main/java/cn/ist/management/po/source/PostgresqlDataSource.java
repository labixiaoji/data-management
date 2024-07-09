package cn.ist.management.po.source;

import cn.ist.management.po.Field;
import cn.ist.management.util.PostgresqlHelper;
import lombok.*;

import java.sql.*;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostgresqlDataSource extends DataSource {

    private String schemaName;
    private String tableName;

    @Override
    public List<Map<String, Object>> connectAndGetData() throws SQLException {

        Connection connection = PostgresqlHelper.getConnection(getUrl());
        assert connection != null;

        List<Map<String, Object>> rows = PostgresqlHelper.getRows(connection, getSchemaName(), getTableName(), null);

        System.out.println("postgresql connectAndGetData rows = " + rows);

        connection.close();

        return rows;

    }

    @Override
    public List<Field> metaFields() throws SQLException {
        Connection connection = PostgresqlHelper.getConnection(getUrl());
        assert connection != null;
        List<Field> metaFields = PostgresqlHelper.getMetaFields(connection, schemaName, tableName);
        connection.close();
        return metaFields;
    }

    @Override
    public List<String> allTables() throws SQLException {
        Connection connection = PostgresqlHelper.getConnection(getUrl());
        assert connection != null;
        List<String> tables = PostgresqlHelper.getAllTables(connection, schemaName);
        connection.close();
        return tables;
    }

}
