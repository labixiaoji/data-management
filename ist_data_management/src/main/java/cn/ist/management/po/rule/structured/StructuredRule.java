package cn.ist.management.po.rule.structured;

import cn.ist.management.po.rule.QualityRule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public abstract class StructuredRule extends QualityRule {
    public static final Integer COMPARISON_RULE = 1;
    public static final Integer NOT_NULL_RULE = 2;
    public static final Integer FIELD_LENGTH_RULE = 3;
}
