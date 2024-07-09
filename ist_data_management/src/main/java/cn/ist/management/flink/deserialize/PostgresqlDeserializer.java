package cn.ist.management.flink.deserialize;

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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Date.toLogical;

public class PostgresqlDeserializer implements DebeziumDeserializationSchema<String> {

    // 日期格式转换时区
    private static final String serverTimeZone = "Asia/Shanghai";

    private String modelId;
    private String sourceId;

    public PostgresqlDeserializer(String modelId, String sourceId) {
        this.modelId = modelId;
        this.sourceId = sourceId;
    }

    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<String> collector) throws Exception {

        System.out.println("sourceRecord: " + sourceRecord);

        // 1. 创建一个JSONObject用来存放最终封装好的数据
        JSONObject result = new JSONObject();

        // 2. 解析主键
        Struct key = (Struct) sourceRecord.key();
        JSONObject keyJs = parseStruct(key);

        // 3. 解析值
        Struct value = (Struct) sourceRecord.value();
        Struct source = value.getStruct("source");

        JSONObject beforeJson = parseStruct(value.getStruct("before"));
        JSONObject afterJson = parseStruct(value.getStruct("after"));

        // 将数据封装到JSONObject中
        result.put("db", source.get("db").toString());
        result.put("modelId", this.modelId);
        result.put("sourceId", this.sourceId);
        result.put("schema", source.get("schema").toString());
        result.put("table", source.get("table").toString());
        result.put("key", keyJs);
        result.put("op", value.get("op").toString());
        result.put("op_ts", LocalDateTime.ofInstant(Instant.ofEpochMilli(source.getInt64("ts_ms")), ZoneId.of(serverTimeZone)));
        result.put("current_ts", LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getInt64("ts_ms")), ZoneId.of(serverTimeZone)));
        result.put("before", beforeJson);
        result.put("after", afterJson);
        result.put("datetime", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 将数据发送至下游
        collector.collect(result.toJSONString());

    }

    private JSONObject parseStruct(Struct valueStruct) {

        if (valueStruct == null) return null;

        JSONObject dataJson = new JSONObject();

        for (Field field : valueStruct.schema().fields()) {

            Object v = valueStruct.get(field);
            String type = field.schema().name();
            Object val = null;

            if (v instanceof Long) {
                long vl = (Long) v;
                val = convertLongToTime(vl, type);
            } else if (v instanceof Integer) {
                int iv = (Integer) v;
                val = convertIntToDate(iv, type);
            } else if (v == null) {
                val = null;
            } else {
                val = convertObjToTime(v, type);
            }

            dataJson.put(field.name(), val);

        }

        return dataJson;

    }

    private Object convertObjToTime(Object obj, String type) {
        Object val = obj;
        if (Time.SCHEMA_NAME.equals(type) || MicroTime.SCHEMA_NAME.equals(type) || NanoTime.SCHEMA_NAME.equals(type)) {
            val = java.sql.Time.valueOf(TemporalConversions.toLocalTime(obj)).toString();
        } else if (Timestamp.SCHEMA_NAME.equals(type) || MicroTimestamp.SCHEMA_NAME.equals(type) || NanoTimestamp.SCHEMA_NAME.equals(type) || ZonedTimestamp.SCHEMA_NAME.equals(type)) {
            val = java.sql.Timestamp.valueOf(TemporalConversions.toLocalDateTime(obj, ZoneId.of(serverTimeZone))).toString();
        }
        return val;
    }

    private Object convertIntToDate(int obj, String type) {
        SchemaBuilder date_schema = SchemaBuilder.int64().name("org.apache.kafka.connect.data.Date");
        Object val = obj;
        if (Date.SCHEMA_NAME.equals(type)) {
            val = toLogical(date_schema, obj).toInstant().atZone(ZoneId.of(serverTimeZone)).toLocalDate().toString();
        }
        return val;
    }

    private Object convertLongToTime(long obj, String type) {
        SchemaBuilder time_schema = SchemaBuilder.int64().name("org.apache.kafka.connect.data.Time");
        SchemaBuilder date_schema = SchemaBuilder.int64().name("org.apache.kafka.connect.data.Date");
        SchemaBuilder timestamp_schema = SchemaBuilder.int64().name("org.apache.kafka.connect.data.Timestamp");
        Object val = obj;
        if (Time.SCHEMA_NAME.equals(type)) {
            val = toLogical(time_schema, (int) obj).toInstant().atZone(ZoneId.of(serverTimeZone)).toLocalTime().toString();
        } else if (MicroTime.SCHEMA_NAME.equals(type)) {
            val = toLogical(time_schema, (int) (obj / 1000)).toInstant().atZone(ZoneId.of(serverTimeZone)).toLocalTime().toString();
        } else if (NanoTime.SCHEMA_NAME.equals(type)) {
            val = toLogical(time_schema, (int) (obj / 1000 / 1000)).toInstant().atZone(ZoneId.of(serverTimeZone)).toLocalTime().toString();
        } else if (Timestamp.SCHEMA_NAME.equals(type)) {
            LocalDateTime t = toLogical(timestamp_schema, (int) obj).toInstant().atZone(ZoneId.of(serverTimeZone)).toLocalDateTime();
            val = java.sql.Timestamp.valueOf(t).toString();
        } else if (MicroTimestamp.SCHEMA_NAME.equals(type)) {
            LocalDateTime t = toLogical(timestamp_schema, (int) obj / 1000).toInstant().atZone(ZoneId.of(serverTimeZone)).toLocalDateTime();
            val = java.sql.Timestamp.valueOf(t).toString();
        } else if (NanoTimestamp.SCHEMA_NAME.equals(type)) {
            LocalDateTime t = toLogical(timestamp_schema, (int) obj / 1000 / 1000).toInstant().atZone(ZoneId.of(serverTimeZone)).toLocalDateTime();
            val = java.sql.Timestamp.valueOf(t).toString();
        } else if (Date.SCHEMA_NAME.equals(type)) {
            val = toLogical(date_schema, (int) obj).toInstant().atZone(ZoneId.of(serverTimeZone)).toLocalDate().toString();
        }
        return val;
    }

    @Override
    public TypeInformation<String> getProducedType() {
        return BasicTypeInfo.STRING_TYPE_INFO;
    }

}
