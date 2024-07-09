package cn.ist.management.po.model;

import cn.ist.management.po.Field;
import cn.ist.management.po.rule.semi_structured.SemiStructuredRule;
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
class SemiStructuredDataModelTest {

    private final SemiStructuredDataModel semiStructuredDataModel;

    public SemiStructuredDataModelTest() {

        semiStructuredDataModel = new SemiStructuredDataModel();

        semiStructuredDataModel.setType(DataModel.TYPE_SEMI_STRUCTURED);
        semiStructuredDataModel.setModelName("cn_test_semi_structured_model");
        semiStructuredDataModel.setModal(DataModel.MODAL_TEMPORAL);
        semiStructuredDataModel.setTag(null);
        semiStructuredDataModel.setDomain("testDomain");
        semiStructuredDataModel.setDescription("test");

        ArrayList<Field> fields = new ArrayList<>();
        fields.add(new Field("model_col1", Field.TYPE_NUMBER, "testDescription", true, true, 0));
        fields.add(new Field("model_col2!#@col21!#@col211", Field.TYPE_NUMBER, "testDescription", true, true, 0));
        fields.add(new Field("model_col3", Field.TYPE_STRING, "testDescription", false, false, 0));
        fields.add(new Field("model_col4", Field.TYPE_NUMBER, "testDescription", false, false, 0));
        semiStructuredDataModel.setFields(fields);

        ArrayList<SemiStructuredRule> rules = new ArrayList<>();
        semiStructuredDataModel.setRules(rules);

        ArrayList<DataSource> sources = new ArrayList<>();

        PostgresqlDataSource postgresqlDataSource = new PostgresqlDataSource();
        postgresqlDataSource.setType(DataSource.SOURCE_TYPE_POSTGRESQL);
        postgresqlDataSource.setUrl("jdbc:postgresql://124.222.140.214:5666/data_management_test_source?user=postgres&password=123qweasd");
        postgresqlDataSource.setSchemaName("data_management_test_source");
        postgresqlDataSource.setTableName("test_source");
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("source_col1", "model_col1");
        fieldMap.put("source_col2", "model_col2!#@col21!#@col211");
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
        fieldMap.put("col2", "model_col2!#@col21!#@col211");
        fieldMap.put("col3", "model_col3");
        fieldMap.put("col4!#@col41!#@col411", "model_col4");
//        mongodbDataSource.setFieldMap(fieldMap);
        sources.add(mongodbDataSource);

        semiStructuredDataModel.setDataSources(sources);

    }

    @Test
    void saveData() throws SQLException {
        semiStructuredDataModel.saveData();
    }

    @Test
    void fetchData() {
//        List<Map<String, Object>> data = semiStructuredDataModel.fetchData(false, );
//        System.out.println(data);
    }

}