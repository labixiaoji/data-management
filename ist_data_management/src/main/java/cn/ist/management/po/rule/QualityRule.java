package cn.ist.management.po.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class QualityRule {

    private String description;

    public abstract void filter(List<Map<String, Object>> rows);

}
