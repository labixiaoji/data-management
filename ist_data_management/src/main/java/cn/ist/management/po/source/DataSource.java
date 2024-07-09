package cn.ist.management.po.source;


import cn.ist.management.po.Field;
import cn.ist.management.po.model.DataModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.SQLException;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data_source")
public abstract class DataSource {

    public static final Integer SOURCE_TYPE_POSTGRESQL = 1;
    public static final Integer SOURCE_TYPE_MONGODB = 2;
    public static final Integer SOURCE_TYPE_DIRECTORY = 3;
    public static final Integer SOURCE_TYPE_INFLUXDB = 4;

    @Id
    private String dataSourceId;
    private Integer type;
    /*
     postgresql：ip port username password databaseName
     mongodb: ip port username password databaseName
     influxdb: ip port username password databaseName
     directory: path
     */
    private String url;
    private String tag;
    private String description;

    abstract public List<Map<String, Object>> connectAndGetData() throws SQLException;

    abstract public List<Field> metaFields() throws SQLException;

    abstract public List<String> allTables() throws SQLException;

}
