package cn.ist.management.po.rule.structured;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonRuleTest {

    @Test
    void filter() {

        ArrayList<Map<String, Object>> rows = new ArrayList<>();

        HashMap<String, Object> row = new HashMap<>();
        row.put("left", 1.0);
        row.put("right", 2.0);
        rows.add(row);

        row = new HashMap<>();
        row.put("left", 1.0);
        row.put("right", 1.0);
        rows.add(row);

        row = new HashMap<>();
        row.put("left", 1.0);
        row.put("right", 0.0);
        rows.add(row);

        System.out.println("before filter: " + rows);
        ComparisonRule rule = new ComparisonRule(StructuredRule.COMPARISON_RULE, "left", "right", ComparisonRule.TYPE_GE);
        rule.filter(rows);
        System.out.println("after filter: " + rows);

    }
}