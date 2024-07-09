package cn.ist.management.po.model;

import cn.ist.management.common.Constant;
import cn.ist.management.util.Converter;
import cn.ist.management.util.Encryptor;
import cn.ist.management.util.PostgresqlHelper;
import cn.ist.management.po.Field;
import cn.ist.management.po.rule.structured.StructuredRule;
import cn.ist.management.po.source.DataSource;
import lombok.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class StructuredDataModel extends DataModel {

    private List<StructuredRule> rules;

    public StructuredDataModel(String id, String modelName, Integer modal, Integer type, List<String> tag, String domain, String description, List<Field> fields, Boolean realtime, List<StructuredRule> rules, List<DataSource> dataSources, List<Map<String, String>> fieldMaps) {
        super(id, modelName, modal, type, tag, domain, description, realtime, fields, dataSources, fieldMaps, 100.0, null);
        this.rules = rules;
    }

    @Override
    public void saveData() throws SQLException {

        Connection connection = PostgresqlHelper.getConnection();
        assert connection != null;

        // 先检查我们的服务器里有没有这张表
        boolean exists = PostgresqlHelper.checkTableExists(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName());

        // 如果不存在，那么建表
        if (!exists) {
            PostgresqlHelper.createTable(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), getFields());
        }

        // 最终要存的rows
        List<Map<String, Object>> finalRows = new ArrayList<>();

        for (int i = 0; i < getDataSources().size(); i++) {

            DataSource source = getDataSources().get(i);

            // todo 目前先获取全量数据
            List<Map<String, Object>> rows = source.connectAndGetData();

            rows = Converter.convertRow(getFieldMaps().get(i), rows, source.getDataSourceId());

            // 根据 rule 做过滤
            for (StructuredRule rule : getRules()) {
                rule.filter(rows);
            }

            finalRows.addAll(rows);

        }

//        // 加密
//        Encryptor.encrypt(getFields(), finalRows);

        System.out.println("StructuredDataModel finalRows = " + finalRows);

        // 开存
        PostgresqlHelper.insertRows(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), finalRows);

        connection.close();

    }

    @Override
    public List<Map<String, Object>> fetchData(Boolean forExport, Map<String, Object> conditions) throws SQLException {

        Connection connection = PostgresqlHelper.getConnection();
        assert connection != null;

        boolean exists = PostgresqlHelper.checkTableExists(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName());

        if (!exists) {
            connection.close();
            return new ArrayList<>();
        }

        List<Map<String, Object>> res = PostgresqlHelper.getRows(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), conditions);

        if (forExport) {
            Encryptor.desensitize(getFields(), res);
        }

        connection.close();

        return res;

    }

    @Override
    public void insertOne(Map<String, Object> row) throws SQLException {

        Connection connection = PostgresqlHelper.getConnection();
        assert connection != null;

        boolean exists = PostgresqlHelper.checkTableExists(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName());
        if (!exists) {
            PostgresqlHelper.createTable(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), getFields());
        }

        // 包一层list就能复用之前的代码了
        ArrayList<Map<String, Object>> container = new ArrayList<>();
        container.add(row);

        // 过滤
        for (StructuredRule rule : getRules()) {
            rule.filter(container);
        }

//        // 加密
//        Encryptor.encrypt(getFields(), container);

        // 开存
        PostgresqlHelper.insertRows(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), container);

        connection.close();

    }

    @Override
    public void deleteOne(Map<String, Object> row) throws SQLException {

        Connection connection = PostgresqlHelper.getConnection();
        assert connection != null;

        boolean exists = PostgresqlHelper.checkTableExists(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName());
        if (!exists) {
            PostgresqlHelper.createTable(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), getFields());
        }

        PostgresqlHelper.deleteRow(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), row);

        connection.close();

    }

    @Override
    public void updateOne(Map<String, Object> filterBy, Map<String, Object> newRow) throws SQLException {

        Connection connection = PostgresqlHelper.getConnection();
        assert connection != null;

        boolean exists = PostgresqlHelper.checkTableExists(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName());
        if (!exists) {
            PostgresqlHelper.createTable(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), getFields());
        }

        // 新row也需要过滤加密
        ArrayList<Map<String, Object>> container = new ArrayList<>();
        container.add(newRow);
        for (StructuredRule rule : getRules()) {
            rule.filter(container);
        }
//        Encryptor.encrypt(getFields(), container);

        PostgresqlHelper.updateRow(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName(), filterBy, newRow);

        connection.close();

    }

    @Override
    public void deleteModel() throws SQLException {
        Connection connection = PostgresqlHelper.getConnection();
        assert connection != null;
        PostgresqlHelper.dropTable(connection, Constant.SYSTEM_SCHEMA_NAME, getModelName());
        connection.close();
    }

}
