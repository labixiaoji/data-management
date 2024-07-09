package cn.ist.management.po.model;

import cn.ist.management.po.Field;
import cn.ist.management.po.rule.structured.StructuredRule;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.po.source.MongodbDataSource;
import cn.ist.management.po.source.PostgresqlDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class StructuredDataModelTest {

    private final StructuredDataModel structuredDataModel;

    public StructuredDataModelTest() {

        structuredDataModel = new StructuredDataModel();

        structuredDataModel.setType(DataModel.TYPE_STRUCTURED);
        structuredDataModel.setModelName("cn_test_structured_model");
        structuredDataModel.setModal(DataModel.MODAL_TEMPORAL);
        structuredDataModel.setTag(null);
        structuredDataModel.setDomain("testDomain");
        structuredDataModel.setDescription("test");

        ArrayList<Field> fields = new ArrayList<>();
        fields.add(new Field("model_col1", Field.TYPE_NUMBER, "testDescription", true, false, 0));
        fields.add(new Field("model_col2", Field.TYPE_NUMBER, "testDescription", false, true, 0));
        fields.add(new Field("model_col3", Field.TYPE_STRING, "testDescription", false, false, 0));
        fields.add(new Field("model_col4", Field.TYPE_NUMBER, "testDescription", false, false, 0));
        structuredDataModel.setFields(fields);

        ArrayList<StructuredRule> rules = new ArrayList<>();
        structuredDataModel.setRules(rules);

        ArrayList<DataSource> sources = new ArrayList<>();

        PostgresqlDataSource postgresqlDataSource = new PostgresqlDataSource();
        postgresqlDataSource.setType(DataSource.SOURCE_TYPE_POSTGRESQL);
        postgresqlDataSource.setUrl("jdbc:postgresql://124.222.140.214:5666/data_management_test_source?user=postgres&password=123qweasd");
        postgresqlDataSource.setSchemaName("data_management_test_source");
        postgresqlDataSource.setTableName("test_source");
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("source_col1", "model_col1");
        fieldMap.put("source_col2", "model_col2");
        fieldMap.put("source_col3", "model_col3");
        fieldMap.put("source_col4", "model_col4");
//        postgresqlDataSource.setFieldMap(fieldMap);
        sources.add(postgresqlDataSource);

        MongodbDataSource mongodbDataSource = new MongodbDataSource();
        mongodbDataSource.setType(DataSource.SOURCE_TYPE_MONGODB);
        mongodbDataSource.setUrl("mongodb://chen:woshizhu1010A@124.222.140.214:27088/data_management");
        mongodbDataSource.setCollectionName("test_mongodb_source");
        fieldMap = new HashMap<>();
        fieldMap.put("col1", "model_col1");
        fieldMap.put("col2", "model_col2");
        fieldMap.put("col3", "model_col3");
        fieldMap.put("col4!#@col41!#@col411", "model_col4");
//        mongodbDataSource.setFieldMap(fieldMap);
        sources.add(mongodbDataSource);

        structuredDataModel.setDataSources(sources);

    }

    @Test
    void saveData() throws SQLException {
        structuredDataModel.saveData();
    }

    @Test
    void fetchData() throws SQLException {
//        List<Map<String, Object>> data = structuredDataModel.fetchData(false, );
//        System.out.println(data);
    }

}