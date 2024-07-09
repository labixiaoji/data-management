package cn.ist.management.vo.fromFront;

import cn.ist.management.po.source.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMetaFieldsFromVo {

    private Integer sourceType;

    private String url;

    // postgresql
    private String schemaName;

    // postgresql influxdb
    private String tableName;

    // mongodb
    private String collectionName;

    public DataSource toDataSource() {

        DataSource source;

        if (sourceType.equals(DataSource.SOURCE_TYPE_POSTGRESQL)) {
            source = new PostgresqlDataSource(schemaName, tableName);
        } else if (sourceType.equals(DataSource.SOURCE_TYPE_MONGODB)) {
            source = new MongodbDataSource(collectionName);
        } else if (sourceType.equals(DataSource.SOURCE_TYPE_DIRECTORY)) {
            source = new DirectoryDataSource();
        } else if (sourceType.equals(DataSource.SOURCE_TYPE_INFLUXDB)) {
            source = new InfluxdbDataSource(tableName);
        } else {
            System.out.println("GetMetaFieldsFromVo.toDataSource 非预期的sourceType");
            return null;
        }

        source.setType(sourceType);
        source.setUrl(url);

        return source;

    }

}
