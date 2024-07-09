package cn.ist.management.vo.fromFront;

import cn.ist.management.po.source.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BindDataSourceFromVO {
    private String modelId;
    private List<String> sourceIds;
    private List<Map<String, String>> fieldMaps;
}
