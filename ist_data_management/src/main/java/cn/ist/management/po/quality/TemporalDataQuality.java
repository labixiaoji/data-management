package cn.ist.management.po.quality;

import cn.ist.management.po.source.InfluxdbDataSource;
import cn.ist.management.po.source.PostgresqlDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class TemporalDataQuality extends DataQuality {
    /* 无效值计算待定义
    *  数据获取待定义 */

    private Integer duplicateRowCount = ILLEGAL_CODE;
    private Integer invalidRowCount = ILLEGAL_CODE;
    private Integer missingRowCount = ILLEGAL_CODE;
    private Integer totalRowCount = ILLEGAL_CODE;
    private List<Map<String, Object>> result = new ArrayList<>();

    public void _getData(){
        if (this.getDataSourceUrl().equals(ILLEGAL_URL)){
            throw new UnsupportedOperationException("没有数据源url");
        }

        InfluxdbDataSource influxdbDataSource = new InfluxdbDataSource();
        influxdbDataSource.setUrl(getDataSourceUrl());
        result = influxdbDataSource.connectAndGetData();

    }
    public TemporalDataQuality(String url) {
        super(url);
        setModalType(ILLEGAL_CODE);
        setDataType(TYPE_TEMPORAL);
    }

    public void calTotalRowCount() {
        if (result.isEmpty())   _getData();

        this.totalRowCount = result.size();
    }

    public void calMissingRowCount() {
        if (result.isEmpty())   _getData();

        int missingCount = 0;

        for (Map<String, Object> map : result) {
            boolean isDefault = false;

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();

                if (value == null) {
                    isDefault = true;
                    break;
                }
            }

            if (isDefault) {
                missingCount++;
            }
        }

        this.missingRowCount = missingCount;
    }

    public void calDuplicateRowCount() {
        class JudgeDuplicationRows {
            public boolean mapsAreEqual(Map<String, Object> map1, Map<String, Object> map2) {
                // 如果两个 Map 的键值对完全相同，则认为它们相等
                return map1.equals(map2);
            }

            // 检查列表中是否包含指定的 Map
            public boolean containsElement(ArrayList<Map<String, Object>> list, Map<String, Object> element) {
                for (Map<String, Object> item : list) {
                    if (mapsAreEqual(item, element)) {
                        return true;
                    }
                }
                return false;
            }
        }

        if (result.isEmpty())   _getData();

        ArrayList<Map<String, Object>> checkedElements = new ArrayList<>();
        int duplicateCount = 0;
        JudgeDuplicationRows judgeDuplicationDocuments = new JudgeDuplicationRows();
        for (Map<String, Object> element : result) {
            if (judgeDuplicationDocuments.containsElement(checkedElements, element)) {
                duplicateCount++;
            } else {
                checkedElements.add(element);
            }
        }

        this.duplicateRowCount = duplicateCount;
    }

    @Override
    public double dataQualityScore() {
        if (result.isEmpty())   _getData();

        if (this.totalRowCount == ILLEGAL_CODE) calTotalRowCount();
        if (this.duplicateRowCount == ILLEGAL_CODE) calDuplicateRowCount();
        if (this.missingRowCount == ILLEGAL_CODE) calMissingRowCount();
        // 无效值计算方式待定义.
        if (this.invalidRowCount == ILLEGAL_CODE) this.invalidRowCount = 0;
        double score = 100.0 - ((double) (duplicateRowCount + invalidRowCount + missingRowCount) / totalRowCount * 100);
        setDataQualityScore(score);
        return score > 0 ? score : 0;
    }
}