package cn.ist.management.po.rule.semi_structured;

import cn.ist.management.po.rule.QualityRule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public abstract class SemiStructuredRule extends QualityRule {
    public static final Integer NOT_NULL_RULE = 1;
    public static final Integer FIELD_LENGTH_RULE = 2;
}
