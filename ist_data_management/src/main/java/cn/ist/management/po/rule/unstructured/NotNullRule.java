package cn.ist.management.po.rule.unstructured;

import java.util.List;
import java.util.Map;

public class NotNullRule extends UnstructuredRule{
    private String fieldName;

    @Override
    public void filter(List<Map<String, Object>> rows) {
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            if (!row.containsKey(fieldName)) {
                rows.remove(i);
                i--;
            }
        }
    }
}
