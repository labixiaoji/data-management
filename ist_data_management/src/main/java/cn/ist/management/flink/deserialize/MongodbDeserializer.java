package cn.ist.management.flink.deserialize;

import cn.ist.management.util.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Field;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.SchemaBuilder;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Struct;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.source.SourceRecord;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import com.ververica.cdc.debezium.utils.TemporalConversions;
import io.debezium.time.*;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.bson.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Date.toLogical;

public class MongodbDeserializer implements DebeziumDeserializationSchema<String> {

    private final String modelId;
    private final String sourceId;

    public MongodbDeserializer(String modelId, String sourceId) {
        this.modelId = modelId;
        this.sourceId = sourceId;
    }

    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<String> collector) throws Exception {

        Struct value = (Struct) sourceRecord.value();
        Struct ns = (Struct) value.get("ns");

        String url = (String) sourceRecord.sourcePartition().get("ns");
//        System.out.println("url = " + url);

        String operationType = (String) value.get("operationType");
//        System.out.println("operationType = " + operationType);

        String documentKey = (String) value.get("documentKey");
//        Document documentKey = Converter.convertMapToDocument(JSON.parseObject((String) value.get("documentKey")).getInnerMap());
//        System.out.println("documentKey = " + documentKey);

        String document = (String) value.get("fullDocument");
//        System.out.println("document = " + document);

        String db = (String) ns.get("db");
//        System.out.println("db = " + db);

        String coll = (String) ns.get("coll");
//        System.out.println("coll = " + coll);

        // 将数据封装到JSONObject中
        JSONObject result = new JSONObject();

        result.put("url", url);
        result.put("modelId", this.modelId);
        result.put("sourceId", this.sourceId);
        result.put("db", db);
        result.put("coll", coll);
        result.put("document", document);
        result.put("documentKey", documentKey);
        result.put("operationType", operationType);
        result.put("datetime", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        collector.collect(result.toJSONString());

    }

    @Override
    public TypeInformation<String> getProducedType() {
        return BasicTypeInfo.STRING_TYPE_INFO;
    }

}
