package cn.ist.management.po.source;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DirectoryResponseDataSourceTest {
    private final DirectoryDataSource dataSource;

    public DirectoryResponseDataSourceTest() {

        dataSource = new DirectoryDataSource();

        dataSource.setType(DataSource.SOURCE_TYPE_DIRECTORY);
        dataSource.setUrl("ftp://dlpuser:rNrKYTX9g7z3RgJRmxWuGHbeu@ftp.dlptest.com/");
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