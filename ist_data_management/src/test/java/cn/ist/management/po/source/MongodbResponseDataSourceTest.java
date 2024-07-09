package cn.ist.management.po.source;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MongodbResponseDataSourceTest {

    private final MongodbDataSource dataSource;

    public MongodbResponseDataSourceTest() {

        dataSource = new MongodbDataSource();

        dataSource.setType(DataSource.SOURCE_TYPE_MONGODB);
        dataSource.setUrl("mongodb://chen:woshizhu1010A@124.222.140.214:27088/data_management");
        dataSource.setCollectionName("test_mongodb_source");

        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("col1", "model_col1");
        fieldMap.put("col2", "model_col2!#@col21!#@col211");
        fieldMap.put("col3", "model_col3");
        fieldMap.put("col4!#@col41!#@col411", "model_col4");

//        dataSource.setFieldMap(fieldMap);

    }

    @Test
    void connectAndGetData() {
        System.out.println(dataSource.connectAndGetData());
    }

    @Test
    void metaFields() {
        System.out.println(dataSource.metaFields());
    }

    @Test
    void allTables() {
        System.out.println(dataSource.allTables());
    }

}