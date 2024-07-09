package cn.ist.management.po.source;

import cn.ist.management.po.Field;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.po.model.SemiStructuredDataModel;
import cn.ist.management.po.rule.semi_structured.NotNullRule;
import cn.ist.management.po.rule.semi_structured.SemiStructuredRule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostgresqlDataSourceTest {

    private final PostgresqlDataSource dataSource;

    public PostgresqlDataSourceTest() {

        dataSource = new PostgresqlDataSource();

        dataSource.setType(DataSource.SOURCE_TYPE_POSTGRESQL);
        dataSource.setUrl("jdbc:postgresql://124.222.140.214:5666/data_management_test_source?user=postgres&password=123qweasd");
        dataSource.setSchemaName("data_management_test_source");
        dataSource.setTableName("test_source");

        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("source_col1", "model_col1");
        fieldMap.put("source_col2", "model_col2!#@col21!#@col211");
        fieldMap.put("source_col3", "model_col3");
        fieldMap.put("source_col4", "model_col4");

//        dataSource.setFieldMap(fieldMap);

    }

    @Test
    void connectAndGetData() throws SQLException {
        System.out.println(dataSource.connectAndGetData());
    }

    @Test
    void metaFields() throws SQLException {
        System.out.println(dataSource.metaFields());
    }

    @Test
    void allTables() throws SQLException {
        System.out.println(dataSource.allTables());
    }

}