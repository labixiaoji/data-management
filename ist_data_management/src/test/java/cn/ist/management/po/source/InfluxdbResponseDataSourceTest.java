package cn.ist.management.po.source;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InfluxdbResponseDataSourceTest {

    private final InfluxdbDataSource dataSource;

    public InfluxdbResponseDataSourceTest() {

        dataSource = new InfluxdbDataSource();

        dataSource.setType(DataSource.SOURCE_TYPE_INFLUXDB);
        dataSource.setUrl("jdbc:influxdb://124.222.140.214:8086/data_management?user=root&password=123456");
        dataSource.setTableName("test_source");

        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("col1", "time");
        fieldMap.put("col2", "water_level");
        fieldMap.put("col3", "location");

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