package cn.ist.management.util;

import cn.ist.management.common.Constant;
import cn.ist.management.po.Field;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.po.source.DataSource;
import com.alibaba.fastjson.JSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class Converter {

    public static void convertDocumentToMap(Map<String, Object> ret, Document document, String prefix) {

        Set<Map.Entry<String, Object>> entries = document.entrySet();

        for (Map.Entry<String, Object> entry : entries) {

            String key = entry.getKey();

            if (key.equals("_class")) {
                continue;
            }

            if (!prefix.equals("")) {
                key = prefix + Constant.MONGODB_FIELD_SPLIT + key;
            }

            Object value = entry.getValue();

            // todo 目前没考虑array的情况
            if (value instanceof Document) {
                convertDocumentToMap(ret, (Document) value, key);
            } else if (value instanceof JSONObject) {
                String oid = ((JSONObject) value).getString("$oid");
                ret.put(key, oid);
            } else if (value instanceof ObjectId) {
                ret.put(key, ((ObjectId) value).toString());
            } else {
                ret.put(key, value);
            }

        }

    }

    // {col3.col32=testtest, col5.col51=1.0, col3.col31=1.0, col5.col52=testtest, col4=3.1415926, col2=test, col5.col53.col531=1.0, col1=1.0, col5.col53.col532=testtest}
    public static Document convertMapToDocument(Map<String, Object> map) {

        Document result = new Document();

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            Document current = result;

            String[] split = entry.getKey().split(Constant.MONGODB_FIELD_SPLIT);

            for (int i = 0; i < split.length - 1; i++) {

                String key = split[i];

                if (!current.containsKey(key)) {
                    current.put(key, new Document());
                }

                current = (Document) current.get(key);

            }

            current.put(split[split.length - 1], entry.getValue());

        }

        return result;

    }

    public static Map<String, String> extractInfoFromUrl(String url, Integer urlType) {

        HashMap<String, String> map = new HashMap<>();

        // jdbc:postgresql://124.222.140.214:5666/data_management_test_source?user=postgres&password=123qweasd
        if (urlType.equals(DataSource.SOURCE_TYPE_POSTGRESQL)) {

            // 124.222.140.214:5666/data_management_test_source?user=postgres&password=123qweasd
            url = url.substring(url.indexOf("//") + 2);
            map.put("host", url.substring(0, url.indexOf(":")));

            // 5666/data_management_test_source?user=postgres&password=123qweasd
            url = url.substring(url.indexOf(":") + 1);
            map.put("port", url.substring(0, url.indexOf("/")));

            // data_management_test_source?user=postgres&password=123qweasd
            url = url.substring(url.indexOf("/") + 1);
            map.put("dbname", url.substring(0, url.indexOf("?")));

            // user=postgres&password=123qweasd
            url = url.substring(url.indexOf("?") + 1);
            for (String pair : url.split("&")) {
                map.put(pair.substring(0, pair.indexOf('=')), pair.substring(pair.indexOf('=') + 1));
            }

        }
        // mongodb://chen:woshizhu1010A@124.222.140.214:27088/data_management
        else if (urlType.equals(DataSource.SOURCE_TYPE_MONGODB)) {

            // chen:woshizhu1010A@124.222.140.214:27088/data_management
            url = url.substring(url.indexOf("//") + 2);
            map.put("user", url.substring(0, url.indexOf(":")));

            // woshizhu1010A@124.222.140.214:27088/data_management
            url = url.substring(url.indexOf(":") + 1);
            map.put("password", url.substring(0, url.indexOf("@")));

            // 124.222.140.214:27088/data_management
            url = url.substring(url.indexOf("@") + 1);
            map.put("host", url.substring(0, url.indexOf(":")));

            // 27088/data_management
            url = url.substring(url.indexOf(":") + 1);
            map.put("port", url.substring(0, url.indexOf("/")));

            // data_management
            url = url.substring(url.indexOf("/") + 1);
            map.put("dbName", url);

        } else {

            System.out.println("extractInfoFromUrl: unhandled urlType");

        }

        return map;

    }

    public static List<Map<String, Object>> convertRow(Map<String, String> fieldMap, List<Map<String, Object>> rows, String sourceId) {
        if (rows.isEmpty()) {
            return rows;
        }
        ArrayList<Map<String, Object>> res = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> converted = convertRow(fieldMap, row, sourceId);
            res.add(converted);
        }
        return res;
    }

    // 去除不需要的field，并把sourceField的名字替换成targetField的
    // 这里的row都是从数据源获取的一手数据
    public static Map<String, Object> convertRow(Map<String, String> fieldMap, Map<String, Object> row, String sourceId) {
        HashMap<String, Object> temp = new HashMap<>();
        temp.put(Constant.MODEL_SOURCE_ID_COL, sourceId);
        if (row.containsKey("_id")) {
            temp.put(Constant.MODEL_DOCUMENT_ID_COL, row.get("_id"));
            row.remove("_id");
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            if (fieldMap.containsKey(key)) {
                temp.put(fieldMap.get(key), entry.getValue());
            }
        }
        return temp;
    }

    public static void filterSystemField(DataModel dataModel) {
        List<Field> fields = dataModel.getFields();
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals(Constant.MODEL_DOCUMENT_ID_COL) || fields.get(i).getName().equals(Constant.MODEL_SOURCE_ID_COL)) {
                fields.remove(i);
                i--;
            }
        }
    }

    public static void filterSystemField(List<DataModel> dataModels) {
        for (DataModel dataModel : dataModels) {
            filterSystemField(dataModel);
        }
    }

}

















