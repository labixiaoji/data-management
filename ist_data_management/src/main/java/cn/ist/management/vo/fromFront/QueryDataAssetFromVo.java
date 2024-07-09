package cn.ist.management.vo.fromFront;


import cn.ist.management.po.model.DataModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryDataAssetFromVo {
    /*
        model的type同时   决定了rule的type（结构、半结构、非结构），但rule还有自己的更加具体的type。
        比如当这里model的type为结构，那么rule就一定是结构化的那些rule了，但结构化的rule还可以分为非空rule、比较rule等等。
        model的type和dataSource的type并没有关联，比如结构化的model也可以使用半结构的mongodb作为数据源。
    */
    private Integer type;
    private String modelName;
    private Integer modal;
    private String tag;
    private String domain;
    private String description;
}
