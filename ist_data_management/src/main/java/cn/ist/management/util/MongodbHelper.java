package cn.ist.management.util;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MongodbHelper {

    private static MongoClient getMongoClient() {
        try {
            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString("mongodb://chen:woshizhu1010A@124.222.140.214:27088/data_management"))
                    .build();
            return MongoClients.create(mongoClientSettings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void insertDocuments(String collectionName, List<Document> documents) throws SQLException {

        if (documents.isEmpty()) {
            return;
        }

        MongoClient mongoClient = getMongoClient();
        assert mongoClient != null;

        MongoDatabase mongoDatabase = mongoClient.getDatabase("data_management");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        mongoCollection.insertMany(documents);

        mongoClient.close();

    }

    public static List<Document> getDocuments(String collectionName, Map<String, Object> conditions) {

        MongoClient mongoClient = getMongoClient();
        assert mongoClient != null;

        MongoDatabase mongoDatabase = mongoClient.getDatabase("data_management");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);


        BasicDBObject query = new BasicDBObject();
        if (conditions != null && !conditions.isEmpty()) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                query.append(entry.getKey(), entry.getValue());
            }
        }

        FindIterable<Document> documents = mongoCollection.find(query);

        ArrayList<Document> result = new ArrayList<>();
        for (Document document : documents) {
            result.add(document);
        }

        mongoClient.close();

        return result;

    }

    public static void updateDocument(String collectionName, Document document) throws SQLException {

        MongoClient mongoClient = getMongoClient();
        assert mongoClient != null;

        MongoDatabase mongoDatabase = mongoClient.getDatabase("data_management");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        mongoCollection.replaceOne(new Document("_id", document.get("_id")), document);

        mongoClient.close();

    }

    public static void deleteDocument(String collectionName, Document document) throws SQLException {

        MongoClient mongoClient = getMongoClient();
        assert mongoClient != null;

        MongoDatabase mongoDatabase = mongoClient.getDatabase("data_management");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        mongoCollection.deleteOne(new Document("_id", document.get("_id")));

        mongoClient.close();

    }

    public static void dropCollection(String collectionName) throws SQLException {

        MongoClient mongoClient = getMongoClient();
        assert mongoClient != null;

        MongoDatabase mongoDatabase = mongoClient.getDatabase("data_management");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        mongoCollection.drop();

        mongoClient.close();

    }

}
