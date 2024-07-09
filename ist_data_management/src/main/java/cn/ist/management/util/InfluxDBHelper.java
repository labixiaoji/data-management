package cn.ist.management.util;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * InfluxDB数据库连接操作类
 */
public class InfluxDBHelper {

//    @Value("${spring.influx.url}")
//    private static String openurl;
//    @Value("${spring.influx.user}")
//    private static String username;
//    @Value("${spring.influx.password}")
//    private static String password;

    private static String openurl = "http://124.222.140.214:8086";
    private static String username = "root";
    private static String password = "123456";
    private static String dbName = "datagovernance";
    private static String retentionPolicy = "autogen";

    public InfluxDBHelper() {
    }

    /**
     * 连接时序数据库
     *
     * @return
     */
    public static InfluxDB influxDbBuilder() {
        InfluxDB influxDB = InfluxDBFactory.connect(openurl, username, password);
        try {
            if (!influxDB.databaseExists(dbName)) {
                influxDB.createDatabase(dbName);
            }
        } catch (Exception e) {
            // 该数据库可能设置动态代理，不支持创建数据库
            e.printStackTrace();
        } finally {
            influxDB.setRetentionPolicy(retentionPolicy);
        }
        influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
        return influxDB;
    }

    public static List<Map<String, Object>> getData(String sql) {
        InfluxDB influxDB = influxDbBuilder();
        QueryResult queryResult = influxDB.query(new Query(sql, dbName));
        List<Map<String, Object>> res = new ArrayList<>();

        for (QueryResult.Result result : queryResult.getResults()) {
            List<QueryResult.Series> seriesList = result.getSeries();

            if (seriesList != null) {
                for (QueryResult.Series series : seriesList) {
                    List<String> columns = series.getColumns();
                    List<List<Object>> values = series.getValues();

                    for (List<Object> row : values) {
                        Map<String, Object> rowMap = new HashMap<>();
                        for (int i = 0; i < columns.size(); i++) {
                            rowMap.put(columns.get(i), row.get(i));
                        }
                        res.add(rowMap);
                    }
                }
            }
        }

        return null;
    }


    public static void insertRows(String modelName, List<Map<String, Object>> finalRows) {
        InfluxDB influxDB = influxDbBuilder();
        for (Map<String, Object> row : finalRows) {
            insert(influxDB, modelName, row);
        }
        System.out.println("insert rows end!");
        influxDB.close();
    }

    public static void insert(InfluxDB influxDB, String modelName, Map<String, Object> row) {

        Point.Builder builder = Point.measurement(modelName);
        builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        builder.fields(row);
        influxDB.write(dbName, "", builder.build());
    }
}