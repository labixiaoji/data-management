package cn.ist.management.util;

import cn.ist.management.po.Field;

import java.sql.*;
import java.util.*;


public class PostgresqlHelper {

    private static String buildWhere(Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return "1 = 1";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                builder.append("\"").append(key).append("\"=").append("'").append(value).append("'");
            } else {
                // ABS(float_column - desired_value) < 0.001;
                builder.append("ABS(\"").append(key).append("\"").append("-").append(value).append(")<0.0001");
            }
            builder.append(" AND ");
        }
        builder.delete(builder.length() - 5, builder.length());
        return builder.toString();
    }

    // 空构造器返回本系统的postgresql连接
    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://124.222.140.214:5666/data_management?user=postgres&password=123qweasd";
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection(String url) {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkTableExists(Connection connection, String schemaName, String tableName) throws SQLException {
        Statement st = connection.createStatement();
        String checkSql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='" + schemaName + "' AND table_name='" + tableName + "');";
        ResultSet resultSet = st.executeQuery(checkSql);
        resultSet.next();
        return resultSet.getBoolean(1);
    }

    public static void createTable(Connection connection, String schemaName, String tableName, List<Field> fields) throws SQLException {

        StringBuilder builder = new StringBuilder("CREATE TABLE \"" + schemaName + "\".\"" + tableName + "\" (");
        for (Field field : fields) {
            builder.append("\"").append(field.getName()).append("\" ").append(field.accordingPostgresqlType()).append(',');
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(");");

        String createSql = builder.toString();
        System.out.println("createSql: " + createSql);

        Statement statement = connection.createStatement();
        statement.execute(createSql);

    }

    public static void dropTable(Connection connection, String schemaName, String tableName) throws SQLException {

        String dropSql = "DROP TABLE IF EXISTS \"" + schemaName + "\".\"" + tableName + "\";";
        System.out.println("dropSql: " + dropSql);

        Statement statement = connection.createStatement();
        statement.execute(dropSql);

    }

    public static void insertRows(Connection connection, String schemaName, String tableName, List<Map<String, Object>> rows) throws SQLException {

        if (rows.isEmpty()) {
            return;
        }

        // 有哪些 field
        ArrayList<String> fieldList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : rows.get(0).entrySet()) {
            fieldList.add(entry.getKey());
        }

        // 先把 fieldStr 做出来
        StringBuilder builder = new StringBuilder("(");
        for (String filed : fieldList) {
            builder.append("\"").append(filed).append("\",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        String fieldStr = builder.toString();

        // 再把 valueStr 做出来
        builder = new StringBuilder();
        for (Map<String, Object> row : rows) {
            builder.append("(");
            for (String fieldName : fieldList) {
                Object val = row.get(fieldName);
                if (val instanceof String) {
                    builder.append("'").append(val).append("'").append(",");
                } else {
                    builder.append(val).append(",");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append("),");
        }
        builder.deleteCharAt(builder.length() - 1);
        String valueStr = builder.toString();

        // 最后做 sql
        String insertSql = "INSERT INTO \"" + schemaName + "\".\"" + tableName + "\" " + fieldStr + " VALUES " + valueStr + ";";
        System.out.println(insertSql);

        Statement statement = connection.createStatement();
        statement.execute(insertSql);

    }

    public static List<Map<String, Object>> getRows(Connection connection, String schemaName, String tableName, Map<String, Object> conditions) throws SQLException {

        ArrayList<Map<String, Object>> result = new ArrayList<>();

        boolean exists = checkTableExists(connection, schemaName, tableName);
        if (!exists) {
            return result;
        }

        // 查询获取表中的所有数据
        String dataSql = "SELECT * FROM \"" + schemaName + "\".\"" + tableName + "\" WHERE " + buildWhere(conditions);
        Statement statement = connection.createStatement();
        ResultSet dataResultSet = statement.executeQuery(dataSql);

        // 获取结果集元数据，包括字段数量
        ResultSetMetaData metaData = dataResultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 遍历结果集并处理数据
        while (dataResultSet.next()) {

            HashMap<String, Object> map = new HashMap<>();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = dataResultSet.getObject(i);
                map.put(columnName, columnValue);
            }

            result.add(map);

        }

        return result;

    }

    public static List<Field> getMetaFields(Connection connection, String schemaName, String tableName) throws SQLException {

        boolean exists = checkTableExists(connection, schemaName, tableName);
        if (!exists) {
            return null;
        }

        String sql = "SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = ? and table_name = ?";
        System.out.println(sql);

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schemaName);
        statement.setString(2, tableName);

        ResultSet resultSet = statement.executeQuery();

        ArrayList<Field> fields = new ArrayList<>();

        while (resultSet.next()) {
            String columnName = resultSet.getString("column_name");
            String type = resultSet.getString("data_type");
            if (type.equals("integer") || type.equals("real") || type.equals("bigint") || type.equals("double precision")) {
                fields.add(new Field(columnName, Field.TYPE_NUMBER));
            } else if (type.equals("character varying") || type.equals("text")) {
                fields.add(new Field(columnName, Field.TYPE_STRING));
            } else {
                System.out.println("PostgresqlHelper.getMetaFields " + type + " 非预期的columnType");
            }
        }

        return fields;

    }

    public static List<String> getAllTables(Connection connection, String schemaName) throws SQLException {

        // todo 理论上得判断schema是否存在，先摆了

        String sql = "SELECT table_schema || '.' || table_name AS full_name\n" +
                "FROM information_schema.tables\n" +
                "WHERE table_type = 'BASE TABLE'\n" +
                "AND table_schema NOT IN ('pg_catalog', 'information_schema');\n";
        System.out.println(sql);

        PreparedStatement statement = connection.prepareStatement(sql);

        ResultSet resultSet = statement.executeQuery();

        ArrayList<String> tables = new ArrayList<>();

        while (resultSet.next()) {
            String tableFullName = resultSet.getString("full_name");
            tables.add(tableFullName);
        }

        System.out.println(tables);

        return tables;

    }

    public static void deleteRow(Connection connection, String schemaName, String tableName, Map<String, Object> rowToDelete) throws SQLException {

        StringBuilder builder = new StringBuilder();

        builder.append("DELETE FROM \"").append(schemaName).append("\".\"").append(tableName).append("\" WHERE ").append(buildWhere(rowToDelete));

        String deleteSql = builder.toString();
        System.out.println(deleteSql);

        Statement statement = connection.createStatement();
        statement.execute(deleteSql);

    }

    public static void updateRow(Connection connection, String schemaName, String tableName, Map<String, Object> filterBy, Map<String, Object> newRow) throws SQLException {

        StringBuilder builder = new StringBuilder();

        builder.append("UPDATE \"").append(schemaName).append("\".\"").append(tableName).append("\" SET ");

        for (Map.Entry<String, Object> entry : newRow.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            builder.append("\"").append(key).append("\"=");
            if (value instanceof String) {
                builder.append("'").append(value).append("'");
            } else {
                builder.append(value);
            }
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);

        builder.append(" WHERE ").append(buildWhere(filterBy));

        String updateSql = builder.toString();
        System.out.println(updateSql);

        Statement statement = connection.createStatement();
        statement.execute(updateSql);

    }

}
