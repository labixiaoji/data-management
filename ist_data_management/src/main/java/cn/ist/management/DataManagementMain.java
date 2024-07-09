package cn.ist.management;

import cn.ist.management.flink.deserialize.MongodbDeserializer;
import com.ververica.cdc.connectors.mongodb.MongoDBSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import io.github.yedaxia.apidocs.Docs;
import io.github.yedaxia.apidocs.DocsConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@SpringBootApplication
@EnableScheduling
public class DataManagementMain {
    public static void main(String[] args) throws Exception {
        try {
            DocsConfig config = new DocsConfig();
            config.setProjectPath("./");
            config.setProjectName("DataManagement");
            config.setApiVersion("V1.0");
            config.setDocsPath("./");
            config.setAutoGenerate(Boolean.TRUE);
            Docs.buildHtmlDocs(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SpringApplication.run(DataManagementMain.class, args);
    }
}