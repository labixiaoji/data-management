package cn.ist.management.po.rule.structured;

import cn.ist.management.po.Field;
import lombok.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonRule extends StructuredRule {

    public static final Integer TYPE_GE = 1;
    public static final Integer TYPE_GT = 2;
    public static final Integer TYPE_EQ = 3;
    public static final Integer TYPE_LE = 4;
    public static final Integer TYPE_LT = 5;

    private Integer ruleType = StructuredRule.COMPARISON_RULE;
    private String leftFieldName;
    private String rightFieldName;
    private Integer type;

    @Override
    public void filter(List<Map<String, Object>> rows) {

        if (rows.isEmpty()) {
            return;
        }

        // 先随便拿一行检查一下数据类型
        Map<String, Object> row = rows.get(0);

        Object leftValue = row.get(leftFieldName);
        Object rightValue = row.get(rightFieldName);

        if (leftValue == null || rightValue == null) {
            return;
        }

        if (leftValue instanceof String || rightValue instanceof String) {
            System.out.println("字符串类型无法进行ComparisonRule");
            return;
        }

        for (int i = 0; i < rows.size(); i++) {

            row = rows.get(i);

            Double leftVal;
            Double rightVal;

            // 统一转换成Double再比较大小
            if (row.get(leftFieldName) instanceof Integer) {
                leftVal = ((Integer) row.get(leftFieldName)).doubleValue();
            } else {
                leftVal = (Double) row.get(leftFieldName);
            }
            if (row.get(rightFieldName) instanceof Integer) {
                rightVal = ((Integer) row.get(rightFieldName)).doubleValue();
            } else {
                rightVal = (Double) row.get(rightFieldName);
            }

            boolean remove = false;

            if (type.equals(TYPE_EQ) && !leftVal.equals(rightVal)) {
                remove = true;
            } else if (type.equals(TYPE_GE) && leftVal.compareTo(rightVal) < 0) {
                remove = true;
            } else if (type.equals(TYPE_GT) && leftVal.compareTo(rightVal) <= 0) {
                remove = true;
            } else if (type.equals(TYPE_LE) && leftVal.compareTo(rightVal) > 0) {
                remove = true;
            } else if (type.equals(TYPE_LT) && leftVal.compareTo(rightVal) >= 0) {
                remove = true;
            }

            if (remove) {
                rows.remove(i);
                i--;
            }

        }

    }

}
