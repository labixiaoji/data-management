package cn.ist.management.vo.fromFront;

import cn.ist.management.po.Field;
import cn.ist.management.po.model.*;
import cn.ist.management.po.rule.semi_structured.NotNullRule;
import cn.ist.management.po.rule.semi_structured.SemiStructuredRule;
import cn.ist.management.po.rule.structured.ComparisonRule;
import cn.ist.management.po.rule.structured.StructuredRule;
import cn.ist.management.po.rule.unstructured.SizeRule;
import cn.ist.management.po.rule.unstructured.UnstructuredRule;
import cn.ist.management.po.source.*;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDataSourceFromVo {

    private Integer sourceType;
    private String url;
    private String tag;
    private String description;
    // postgresql
    private String schemaName;
    // mongodb
    private String collectionName;
    // postgresql influxdb
    private String tableName;

    public DataSource toPo() {

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
        source.setTag(tag);
        source.setDescription(description);

        return source;

    }

}
