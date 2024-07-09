package cn.ist.management.po.source;

import cn.ist.management.common.Constant;
import cn.ist.management.po.Field;
import cn.ist.management.util.Converter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import lombok.*;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MongodbDataSource extends DataSource {

    private String collectionName;

    @Override
    public List<Map<String, Object>> connectAndGetData() {

        MongoClient mongoClient = null;

        try {

            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(getUrl()))
                    .build();
            mongoClient = MongoClients.create(mongoClientSettings);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(getUrl().substring(getUrl().lastIndexOf('/') + 1));
            MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

            FindIterable<Document> documents = mongoCollection.find();

            List<Map<String, Object>> result = new ArrayList<>();
            for (Document document : documents) {
                HashMap<String, Object> map = new HashMap<>();
                Converter.convertDocumentToMap(map, document, "");
                result.add(map);
            }

            System.out.println(result);

            System.out.println("mongodb connectAndGetData result = " + result);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mongoClient != null) mongoClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;

    }

    // 前后端交互时分隔符都使用"!^@"，只有前端展示的时候转换为"."
    @Override
    public List<Field> metaFields() {

        MongoClient mongoClient = null;

        try {

            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(getUrl()))
                    .build();
            mongoClient = MongoClients.create(mongoClientSettings);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(getUrl().substring(getUrl().lastIndexOf('/') + 1));
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            FindIterable<Document> temp = collection.find().limit(1);

            Map<String, Object> map = new HashMap<>();
            for (Document document : temp) {
                Converter.convertDocumentToMap(map, document, "");
                break;
            }

            ArrayList<Field> fields = new ArrayList<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Double || value instanceof Integer) {
                    fields.add(new Field(key, Field.TYPE_NUMBER));
                } else if (value instanceof String) {
                    fields.add(new Field(key, Field.TYPE_STRING));
                } else {
                    System.out.println("MongodbDataSource.getMetaFields：非预期的valueType");
                }
            }

            System.out.println(fields);

            return fields;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mongoClient != null) mongoClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;

    }

    @Override
    public List<String> allTables() {

        MongoClient mongoClient = null;

        try {

            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(getUrl()))
                    .build();
            mongoClient = MongoClients.create(mongoClientSettings);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(getUrl().substring(getUrl().lastIndexOf('/') + 1));

            MongoIterable<String> collectionNames = mongoDatabase.listCollectionNames();

            ArrayList<String> result = new ArrayList<>();
            for (String name : collectionNames) {
                result.add(name);
            }

            System.out.println(result);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mongoClient != null) mongoClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;

    }

}
