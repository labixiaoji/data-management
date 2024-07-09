package cn.ist.management.po.rule.semi_structured;


import cn.ist.management.po.rule.structured.StructuredRule;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FieldLengthRule extends SemiStructuredRule {

    private Integer ruleType = SemiStructuredRule.FIELD_LENGTH_RULE;

    private String fieldName;
    private Integer min = Integer.MIN_VALUE;
    private Integer max = Integer.MAX_VALUE;

    @Override
    public void filter(List<Map<String, Object>> rows) {

        if (rows.isEmpty()) {
            return;
        }

        Map<String, Object> row = rows.get(0);

        Object value = row.get(fieldName);

        if (value == null) {
            return;
        }

        if (!(value instanceof String)) {
            System.out.println("非字符串类型无法进行FieldLengthRule");
            return;
        }

        for (int i = 0; i < rows.size(); i++) {

            row = rows.get(i);

            String val = (String) row.get(fieldName);

            if (!(val.length() >= min && val.length() <= max)) {
                rows.remove(i);
                i--;
            }

        }

    }

}
