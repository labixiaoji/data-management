package cn.ist.management.flink;

import cn.ist.management.common.Constant;
import cn.ist.management.dao.impl.DataModelDao;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.po.source.MongodbDataSource;
import cn.ist.management.po.source.PostgresqlDataSource;
import cn.ist.management.util.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import org.bson.Document;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaConsumer {

    @Resource
    private DataModelDao dataModelDao;

    @KafkaListener(topics = {Constant.POSTGRESQL_TOPIC})
    public void onPostgresqlMessage(ConsumerRecord<String, Object> record) throws SQLException {

        System.out.println("onPostgresqlMessage");

        JSONObject value = JSON.parseObject((String) record.value());

        String db = value.getString("db");
        String schema = value.getString("schema");
        String table = value.getString("table");
        String modelId = value.getString("modelId");
        String sourceId = value.getString("sourceId");

        PostgresqlDataSource dataSource = null;
        int idx = -1;
        DataModel model = dataModelDao.findByModelId(modelId);
        for (int i = 0; i < model.getDataSources().size(); i++) {
            DataSource source = model.getDataSources().get(i);
            if (source.getDataSourceId().equals(sourceId)) {
                dataSource = (PostgresqlDataSource) source;
                idx = i;
                break;
            }
        }
        assert dataSource != null;

        // 更新lastUpdateTime
        // todo 反复更新意义不大，应该聚合之后统一进行一次更新
        String datetime = value.getString("datetime");
        model.setLastUpdateTime(datetime);
        dataModelDao.updateModel(model);

        String op = value.getString("op");

        switch (op) {
            // 原有and新增
            case "r":
            case "c": {
                Map<String, Object> after = value.getJSONObject("after").getInnerMap();
                after = Converter.convertRow(model.getFieldMaps().get(idx), after, sourceId);
                model.insertOne(after);
                break;
            }
            // 删除
            case "d": {
                Map<String, Object> before = value.getJSONObject("before").getInnerMap();
                before = Converter.convertRow(model.getFieldMaps().get(idx), before, sourceId);
                model.deleteOne(before);
                break;
            }
            // 更新
            case "u": {
                Map<String, Object> before = value.getJSONObject("before").getInnerMap();
                Map<String, Object> after = value.getJSONObject("after").getInnerMap();
                before = Converter.convertRow(model.getFieldMaps().get(idx), before, sourceId);
                after = Converter.convertRow(model.getFieldMaps().get(idx), after, sourceId);
                model.updateOne(before, after);
                break;
            }
            default:
                System.out.println("onPostgresqlMessage: unexpected op");
                break;
        }

    }

    @KafkaListener(topics = {Constant.MONGODB_TOPIC})
    public void onMongodbMessage(ConsumerRecord<String, Object> record) throws SQLException {

        System.out.println("onMongodbMessage");

        JSONObject value = JSON.parseObject((String) record.value());

        String db = value.getString("db");
        String collection = value.getString("coll");
        String modelId = value.getString("modelId");
        String sourceId = value.getString("sourceId");

        MongodbDataSource dataSource = null;
        int idx = -1;
        DataModel model = dataModelDao.findByModelId(modelId);
        for (int i = 0; i < model.getDataSources().size(); i++) {
            DataSource source = model.getDataSources().get(i);
            if (source.getDataSourceId().equals(sourceId)) {
                dataSource = (MongodbDataSource) source;
                idx = i;
                break;
            }
        }
        assert dataSource != null;

        // 更新lastUpdateTime
        // todo 反复更新意义不大，应该聚合之后统一进行一次更新
        String datetime = value.getString("datetime");
        model.setLastUpdateTime(datetime);
        dataModelDao.updateModel(model);

        String op = value.getString("operationType");

        switch (op) {
            // 新增
            case "insert": {
                Document document = Converter.convertMapToDocument(JSON.parseObject(value.getString("document")).getInnerMap());
                Map<String, Object> map = new HashMap<>();
                Converter.convertDocumentToMap(map, document, "");
                map = Converter.convertRow(model.getFieldMaps().get(idx), map, sourceId);
                model.insertOne(map);
                break;
            }
            // 删除
            case "delete": {
                Document document = Converter.convertMapToDocument(JSON.parseObject(value.getString("documentKey")).getInnerMap());
                Map<String, Object> map = new HashMap<>();
                Converter.convertDocumentToMap(map, document, "");
                map = Converter.convertRow(model.getFieldMaps().get(idx), map, sourceId);
                model.deleteOne(map);
                break;
            }
            // 更新
            case "update": {
                Document document = Converter.convertMapToDocument(JSON.parseObject(value.getString("document")).getInnerMap());
                Map<String, Object> map = new HashMap<>();
                Converter.convertDocumentToMap(map, document, "");
                HashMap<String, Object> filterBy = new HashMap<>();
                filterBy.put(Constant.MODEL_DOCUMENT_ID_COL, map.get("_id"));
                map = Converter.convertRow(model.getFieldMaps().get(idx), map, sourceId);
                model.updateOne(filterBy, map);
                break;
            }
            default:
                System.out.println("onMongodbMessage: unexpected op");
                break;
        }

    }

}














