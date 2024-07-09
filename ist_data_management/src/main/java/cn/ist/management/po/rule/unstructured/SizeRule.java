package cn.ist.management.po.rule.unstructured;

import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SizeRule extends UnstructuredRule {

    private Integer min;
    private Integer max;

    @Override
    public void filter(List<Map<String, Object>> rows) {

    }

}
