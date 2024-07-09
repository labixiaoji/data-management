package cn.ist.management.po.source;

import cn.ist.management.po.Field;
import com.alibaba.fastjson.JSON;
//import javafx.beans.binding.ObjectExpression;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class InfluxdbDataSource extends DataSource {

//    private String tableName;
//    private List<Field> sourceFields;

    private String tableName;

    @Override
    public List<Map<String, Object>> connectAndGetData() {
        // 连接数据库
        String url = getUrl();

        Map<String, String> information = getInformation(url);
        String openurl = information.get("openurl");
        String username = information.get("username");
        String password = information.get("password");
        String dbName = information.get("dbName");

        try {
            // 连接数据库
            InfluxDB influxDB = InfluxDBFactory.connect(openurl, username, password);
            influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
            String command = buildQuery();
            // 查询
            QueryResult queryResult = influxDB.query(new Query(command, dbName));
            // 替换
            List<Map<String, Object>> res = processQueryResult(queryResult);
            return res;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private Map<String,String> getInformation(String jdbcUrl){
        Map<String, String> res = new HashMap<>();

        String openurl = null;
        String username = null;
        String password = null;
        String dbName = null;

        // 从JDBC URL中提取主机和端口信息
        try {

            // 定义正则表达式
            String regex = "jdbc:(\\w+)://([^:/]+):(\\d+)/(\\w+)\\?user=([^&]+)&password=([^&]+)";
            Pattern pattern = Pattern.compile(regex);

            // 匹配正则表达式
            Matcher matcher = pattern.matcher(jdbcUrl);

            // 提取匹配结果
            if (matcher.matches()) {
                String scheme = matcher.group(1);
                String host = matcher.group(2);
                int port = Integer.parseInt(matcher.group(3));
                dbName = matcher.group(4);
                username = matcher.group(5);
                password = matcher.group(6);
                // 拼接
                openurl = "http://" + host + ":" + port;

            } else {
                System.out.println("URL doesn't match the pattern.");
            }

            log.info("------------------InfluxDB URL解析-----------------------");
            res.put("openurl", openurl);
            res.put("username", username);
            res.put("password", password);
            res.put("dbName", dbName);
            log.info(res.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<Field> metaFields() {
        List<Field> res = new ArrayList<>();
        String tagQuery = "SHOW TAG KEYS FROM " + tableName;
        String fieldQuery = "SHOW FIELD KEYS FROM " + tableName;
        String url = getUrl();
        Map<String, String> information = getInformation(url);
        String openurl = information.get("openurl");
        String username = information.get("username");
        String password = information.get("password");
        String dbName = information.get("dbName");
        InfluxDB influxDB = InfluxDBFactory.connect(openurl, username, password);
        QueryResult tagResult = influxDB.query(new Query(tagQuery, dbName));
        QueryResult fieldResult = influxDB.query(new Query(fieldQuery, dbName));
        try {
            for (QueryResult.Result result : tagResult.getResults()) {
                for (QueryResult.Series series : result.getSeries()) {
                    List<List<Object>> values = series.getValues();
                    for (List<Object> columnValues : values) {
                        for (Object tmp : columnValues) {
                            // tag数据类型给只有String
                            res.add(new Field((String) tmp, Field.TYPE_STRING));
                        }
                    }
                }
            }
            for (QueryResult.Result result : fieldResult.getResults()) {
                for (QueryResult.Series series : result.getSeries()) {
                    List<List<Object>> values = series.getValues();
                    for (List<Object> columnValues : values) {
                        String columnName = (String) columnValues.get(0);
                        String columnType = (String) columnValues.get(1);
                        // field 有多种数据类型，目前这些应该都在number里，剩下的全部放String
                        if (Objects.equals(columnType, "int") || Objects.equals(columnType, "float") || Objects.equals(columnType, "boolean")) {
                            res.add(new Field(columnName, Field.TYPE_NUMBER));
                        } else {
                            res.add(new Field(columnName, Field.TYPE_STRING));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // InfluxDB 还多一列查不出来的time
        res.add(new Field("time", Field.TYPE_STRING));

        return res;
    }

    @Override
    public List<String> allTables() {
        List<String> res = new ArrayList<>();
        String query = "SHOW MEASUREMENTS";
        String url = getUrl();
        Map<String, String> information = getInformation(url);
        String openurl = information.get("openurl");
        String username = information.get("username");
        String password = information.get("password");
        String dbName = information.get("dbName");
        InfluxDB influxDB = InfluxDBFactory.connect(openurl, username, password);
        QueryResult queryResult = influxDB.query(new Query(query, dbName));
        try {
            for (QueryResult.Result result : queryResult.getResults()) {
                for (QueryResult.Series series : result.getSeries()) {
                    List<List<Object>> values = series.getValues();
                    for (List<Object> columnValues : values) {
                        for (Object tmp : columnValues) {
                            res.add((String) tmp);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private String buildQuery() {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName);

        // 增加tags/field进sql(SELECT tag1, tag2, field1, field2 FROM measurement_name)
//        for (String fieldName : getFieldMap().keySet()) {
//            queryBuilder.append(fieldName).append(", ");
//        }
//        queryBuilder.setLength(queryBuilder.length() - 2);
//        queryBuilder.append(" FROM ").append(tableName);

        return queryBuilder.toString();
    }

    private List<Map<String, Object>> processQueryResult(QueryResult queryResult) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (QueryResult.Result result : queryResult.getResults()) {
            for (QueryResult.Series series : result.getSeries()) {
                List<String> columns = series.getColumns();
                List<List<Object>> values = series.getValues();

//                columns.replaceAll(key -> getFieldMap().get(key));

                Map<String, Object> row = new HashMap<>();
                for (List<Object> columnValues : values) {
                    for (int i = 0; i < columns.size(); i++) {
                        row.put(columns.get(i), columnValues.get(i));
                    }
                    dataList.add(row);
                }
            }
        }
        return dataList; // Return the processed result
    }

}
