package cn.ist.management.vo.fromFront;

import cn.ist.management.po.Field;
import cn.ist.management.po.model.*;
import cn.ist.management.po.rule.semi_structured.NotNullRule;
import cn.ist.management.po.rule.semi_structured.SemiStructuredRule;
import cn.ist.management.po.rule.structured.ComparisonRule;
import cn.ist.management.po.rule.structured.FieldLengthRule;
import cn.ist.management.po.rule.structured.StructuredRule;
import cn.ist.management.po.rule.unstructured.SizeRule;
import cn.ist.management.po.rule.unstructured.UnstructuredRule;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
class RuleFromVo {
    private Integer ruleType;
    // 不同rule的数据结构不同，需要看具体的class
    private Object rule;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddModelFromVo {

    /*
         model的type同时决定了rule的type（结构、半结构、非结构），但rule还有自己的更加具体的type。
         比如当这里model的type为结构，那么rule就一定是结构化的那些rule了，但结构化的rule还可以分为非空rule、比较rule等等。
         model的type和dataSource的type并没有关联，比如结构化的model也可以使用半结构的mongodb作为数据源。
     */
    private Integer type;
    private String modelName;
    private Integer modal;
    private String domain;
    private String description;
    private Boolean realtime;
    private List<String> tag;
    // 非结构不用传
    private List<Field> fields;
    private List<RuleFromVo> rules;

    public DataModel toPo() {

//        // 需要加密的字段只能用字符串存，因为加密后的数据中存在'*'
//        for (Field field : fields) {
//            if (field.getEncrypt()) {
//                field.setType(Field.TYPE_STRING);
//            }
//        }

        if (type.equals(DataModel.TYPE_STRUCTURED)) {

            StructuredDataModel dataModel = new StructuredDataModel(null, modelName, modal, type, tag, domain, description, fields, realtime, null, null, null);

            ArrayList<StructuredRule> structuredRules = new ArrayList<>();
            for (RuleFromVo rule : rules) {
                if (rule.getRuleType().equals(StructuredRule.COMPARISON_RULE)) {
                    structuredRules.add(JSON.parseObject(JSON.toJSONString(rule.getRule()), ComparisonRule.class));
                } else if (rule.getRuleType().equals(StructuredRule.NOT_NULL_RULE)) {
                    structuredRules.add(JSON.parseObject(JSON.toJSONString(rule.getRule()), cn.ist.management.po.rule.structured.NotNullRule.class));
                } else if (rule.getRuleType().equals(StructuredRule.FIELD_LENGTH_RULE)) {
                    structuredRules.add(JSON.parseObject(JSON.toJSONString(rule.getRule()), FieldLengthRule.class));
                } else {
                    System.out.println("AddModelFromVo.toPo：非预期的ruleType");
                }
            }
            dataModel.setRules(structuredRules);

            return dataModel;

        }

        if (type.equals(DataModel.TYPE_SEMI_STRUCTURED)) {

            SemiStructuredDataModel dataModel = new SemiStructuredDataModel(null, modelName, modal, type, tag, domain, description, realtime, fields, null, null, null);

            ArrayList<SemiStructuredRule> semiStructuredRules = new ArrayList<>();
            for (RuleFromVo rule : rules) {
                if (rule.getRuleType().equals(SemiStructuredRule.NOT_NULL_RULE)) {
                    semiStructuredRules.add(JSON.parseObject(JSON.toJSONString(rule.getRule()), NotNullRule.class));
                } else {
                    System.out.println("AddModelFromVo.toPo：非预期的ruleType");
                }
            }
            dataModel.setRules(semiStructuredRules);

            return dataModel;

        }

        if (type.equals(DataModel.TYPE_UNSTRUCTURED)) {

            UnstructuredDataModel dataModel = new UnstructuredDataModel(null, modelName, modal, type, tag, domain, description, realtime, fields, null, null, null);

            ArrayList<UnstructuredRule> unstructuredRules = new ArrayList<>();
            for (RuleFromVo rule : rules) {
                if (rule.getRuleType().equals(UnstructuredRule.SIZE_RULE)) {
                    unstructuredRules.add(JSON.parseObject(JSON.toJSONString(rule.getRule()), SizeRule.class));
                } else {
                    System.out.println("AddModelFromVo.toPo：非预期的ruleType");
                }
            }
            dataModel.setRules(unstructuredRules);

            return dataModel;

        }

        System.out.println("AddModelFromVo.toPo：非预期的dataModelType");

        return null;

    }

}
