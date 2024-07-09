package cn.ist.management.vo.toFront;

import cn.ist.management.po.Field;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.po.source.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetModelDataToVo {
    private String modelId;
    private List<Field> fields;
    private List<Map<String, Object>> data;
    private DataModel model;
}
