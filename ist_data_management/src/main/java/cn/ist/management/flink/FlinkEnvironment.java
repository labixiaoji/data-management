package cn.ist.management.flink;

import cn.ist.management.common.Constant;
import cn.ist.management.flink.deserialize.MongodbDeserializer;
import cn.ist.management.flink.deserialize.PostgresqlDeserializer;
import cn.ist.management.po.source.MongodbDataSource;
import cn.ist.management.po.source.PostgresqlDataSource;
import cn.ist.management.util.Converter;
import com.ververica.cdc.connectors.mongodb.MongoDBSource;
import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

// todo 现在的情况，如果java程序崩溃了，flink任务就全终止了，并且java程序重启后并不会自动重启flink任务
public class FlinkEnvironment {

    private static FlinkKafkaProducer createProducer(String topic) {
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "124.222.140.214:9092");
        kafkaProps.setProperty("transaction.max.timeout.ms", "90000");
        return new FlinkKafkaProducer<>(topic, new SimpleStringSchema(), kafkaProps);
    }

    public static void addPostgresqlSource(PostgresqlDataSource dataSource, String modelId) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Map<String, String> urlInfo = Converter.extractInfoFromUrl(dataSource.getUrl(), dataSource.getType());

        Properties properties = new Properties();
        properties.setProperty("snapshot.mode", "initial");
        properties.setProperty("debezium.slot.drop.on.stop", "true");
        properties.setProperty("include.schema.changes", "true");

        SourceFunction<String> sourceFunction = PostgreSQLSource.<String>builder()
                .hostname(urlInfo.get("host"))
                .port(Integer.parseInt(urlInfo.get("port")))
                .database(urlInfo.get("dbname"))
                .schemaList(dataSource.getSchemaName())
                .tableList(dataSource.getSchemaName() + "." + dataSource.getTableName())
                .username(urlInfo.get("user"))
                .password(urlInfo.get("password"))
                .decodingPluginName("pgoutput")
                .deserializer(new PostgresqlDeserializer(modelId, dataSource.getDataSourceId()))
                .debeziumProperties(properties)
                .slotName("pg_cdc_" + dataSource.getDataSourceId())
                .build();
        DataStreamSource<String> pgDataStream = env.addSource(sourceFunction).setParallelism(1);

        pgDataStream.addSink(createProducer(Constant.POSTGRESQL_TOPIC));

        env.execute(dataSource.getUrl());

    }

    public static void addMongodbSource(MongodbDataSource dataSource, String modelId) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Map<String, String> urlInfo = Converter.extractInfoFromUrl(dataSource.getUrl(), dataSource.getType());

        SourceFunction<String> sourceFunction = MongoDBSource.<String>builder()
                .hosts(urlInfo.get("host") + ":" + urlInfo.get("port"))
                .username(urlInfo.get("user"))
                .password(urlInfo.get("password"))
                .databaseList(urlInfo.get("dbName"))
                .collectionList(urlInfo.get("dbName") + "." + dataSource.getCollectionName())
                .deserializer(new MongodbDeserializer(modelId, dataSource.getDataSourceId()))
                .build();

        DataStreamSource<String> pgDataStream = env.addSource(sourceFunction).setParallelism(1);

        pgDataStream.addSink(createProducer(Constant.MONGODB_TOPIC));

        env.execute(dataSource.getUrl());

    }

}
