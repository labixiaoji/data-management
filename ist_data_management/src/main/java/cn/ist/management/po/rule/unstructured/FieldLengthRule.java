package cn.ist.management.po.rule.unstructured;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//字段长度不能超过20
public class FieldLengthRule extends UnstructuredRule{
    private String fieldName;

    @Override
    public void filter(List<Map<String, Object>> rows) {
        List<Map<String, Object>> filteredRows = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Object fieldValue = row.get(fieldName);
            if (fieldValue instanceof String) {
                String fieldValueString = (String) fieldValue;
                if (fieldValueString.length() <= 20) {
                    filteredRows.add(row);
                }
            } else {
                filteredRows.add(row); // 包括non-string的字段
            }
        }
        rows.clear();
        // 添加到原列中
        rows.addAll(filteredRows);
    }
}
