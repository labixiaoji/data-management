package cn.ist.management.po.model;

import cn.ist.management.po.Field;
import cn.ist.management.po.rule.semi_structured.SemiStructuredRule;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.util.Converter;
import cn.ist.management.util.Encryptor;
import cn.ist.management.util.MongodbHelper;
import lombok.*;
import org.bson.Document;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class SemiStructuredDataModel extends DataModel {

    private List<SemiStructuredRule> rules;

    public SemiStructuredDataModel(String id, String modelName, Integer modal, Integer type, List<String> tag, String domain, String description, Boolean realtime, List<Field> fields, List<SemiStructuredRule> rules, List<DataSource> dataSources, List<Map<String, String>> fieldMaps) {
        super(id, modelName, modal, type, tag, domain, description, realtime, fields, dataSources, fieldMaps, 100.0, null);
        this.rules = rules;
    }

    @Override
    public void saveData() throws SQLException {

        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < getDataSources().size(); i++) {

            DataSource source = getDataSources().get(i);

            List<Map<String, Object>> rows = source.connectAndGetData();

            rows = Converter.convertRow(getFieldMaps().get(i), rows, source.getDataSourceId());

            // 根据 rule 做过滤
            for (SemiStructuredRule rule : getRules()) {
                rule.filter(rows);
            }

//            // 加密
//            Encryptor.encrypt(getFields(), rows);

            // 重新组装为 document
            for (Map<String, Object> row : rows) {
                documents.add(Converter.convertMapToDocument(row));
            }

        }

        MongodbHelper.insertDocuments(getModelName(), documents);

    }

    @Override
    public List<Map<String, Object>> fetchData(Boolean forExport, Map<String, Object> conditions) {

        List<Document> documents = MongodbHelper.getDocuments(getModelName(), conditions);

        ArrayList<Map<String, Object>> result = new ArrayList<>();

        for (Document document : documents) {
            HashMap<String, Object> temp = new HashMap<>();
            Converter.convertDocumentToMap(temp, document, "");
            result.add(temp);
        }

        if (forExport) {
            Encryptor.desensitize(getFields(), result);
        }

        System.out.println(result);

        return result;

    }

    @Override
    public void insertOne(Map<String, Object> row) throws SQLException {

        ArrayList<Map<String, Object>> container = new ArrayList<>();
        container.add(row);

        for (SemiStructuredRule rule : getRules()) {
            rule.filter(container);
        }

//        Encryptor.encrypt(getFields(), container);

        ArrayList<Document> documents = new ArrayList<>();
        documents.add(Converter.convertMapToDocument(container.get(0)));

        MongodbHelper.insertDocuments(getModelName(), documents);

    }

    @Override
    public void deleteOne(Map<String, Object> row) throws SQLException {
        MongodbHelper.deleteDocument(getModelName(), Converter.convertMapToDocument(row));
    }

    @Override
    public void updateOne(Map<String, Object> filterBy, Map<String, Object> newRow) throws SQLException {

        ArrayList<Map<String, Object>> container = new ArrayList<>();
        container.add(newRow);

        for (SemiStructuredRule rule : getRules()) {
            rule.filter(container);
        }

//        Encryptor.encrypt(getFields(), container);

        MongodbHelper.updateDocument(getModelName(), Converter.convertMapToDocument(container.get(0)));

    }

    @Override
    public void deleteModel() throws SQLException {
        MongodbHelper.dropCollection(getModelName());
    }

}
