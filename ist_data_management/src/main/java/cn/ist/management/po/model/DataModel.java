package cn.ist.management.po.model;

import cn.ist.management.po.Field;
import cn.ist.management.po.source.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data_model")
public abstract class DataModel {

    public static final Integer MODAL_TEMPORAL = 1;
    public static final Integer MODAL_IMAGE = 2;
    public static final Integer MODAL_TEXT = 3;
    public static final Integer TYPE_STRUCTURED = 1;
    public static final Integer TYPE_SEMI_STRUCTURED = 2;
    public static final Integer TYPE_UNSTRUCTURED = 3;

    @Id
    private String id;
    private String modelName;
    private Integer modal;
    private Integer type;
    private List<String> tag;
    private String domain;
    private String description;
    private Boolean realtime;
    private List<Field> fields;
    private List<DataSource> dataSources;
    // sourceField : targetField，和dataSources一一对应
    private List<Map<String, String>> fieldMaps;
    private Double qualityScore = 0.0;
    private String lastUpdateTime;

    public abstract void saveData() throws SQLException;

    public abstract List<Map<String, Object>> fetchData(Boolean forExport, Map<String, Object> conditions) throws SQLException;

    public abstract void deleteModel() throws SQLException;

    // 以下三个方法用于实时采集
    public abstract void insertOne(Map<String, Object> row) throws SQLException;

    public abstract void deleteOne(Map<String, Object> row) throws SQLException;

    public abstract void updateOne(Map<String, Object> filterBy, Map<String, Object> newRow) throws SQLException;

}
