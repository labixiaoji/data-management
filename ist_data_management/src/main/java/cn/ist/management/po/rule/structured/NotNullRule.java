package cn.ist.management.po.rule.structured;

import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotNullRule extends StructuredRule {

    private Integer ruleType = StructuredRule.NOT_NULL_RULE;

    private String fieldName;

    @Override
    public void filter(List<Map<String, Object>> rows) {
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            if (!row.containsKey(fieldName) || row.get(fieldName) == null) {
                rows.remove(i);
                i--;
            }
        }
    }

}
