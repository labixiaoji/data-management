package cn.ist.management.po.quality;

import cn.ist.management.po.source.MongodbDataSource;
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
public class SemiStructuredDataQuality extends DataQuality {

    /*  数据获取待定义 */

    private int totalDocuments = ILLEGAL_CODE;
    private int missingFieldDocuments = ILLEGAL_CODE;
    private int duplicateDocuments = ILLEGAL_CODE;
    private List<Map<String, Object>> result = new ArrayList<>();

    private String dataCollectionName = ILLEGAL_URL;

    private void _getData(){
        if (getDataSourceUrl().equals(ILLEGAL_URL)){
            throw new UnsupportedOperationException("没有数据源url");
        }

        if (getDataCollectionName().equals(ILLEGAL_URL)){
            throw new UnsupportedOperationException("没有Collection名称");
        }

        MongodbDataSource mongodbDataSource = new MongodbDataSource();
        mongodbDataSource.setUrl(getDataSourceUrl());
        mongodbDataSource.setCollectionName(getDataCollectionName());

        result = mongodbDataSource.connectAndGetData();
    }

    public SemiStructuredDataQuality(String url) {
        super(url);
        setModalType(ILLEGAL_CODE);
        setDataType(TYPE_SEMI_STRUCTURED);
    }

    public SemiStructuredDataQuality(String url, String collectionName) {
        super(url);
        dataCollectionName = collectionName;
        setModalType(ILLEGAL_CODE);
        setDataType(TYPE_SEMI_STRUCTURED);
    }

    public void calTotalDocuments(){
        if (result.isEmpty())   _getData();

        this.totalDocuments = result.size();
    }
    public void calMissingFieldDocuments(){
        class JudgeMissingFieldDocuments{
            public boolean hasMissingValue(Map<String, Object> map) {
                for (Object value : map.values()) {
                    if (value instanceof String && (value.equals("default") || value.equals("default"))) {
                        return true;
                    } else if (value instanceof Map<?, ?>) {
                        if (hasMissingValue((Map<String, Object>) value)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }

        if (result.isEmpty()) _getData();

        int missingCount = 0;
        JudgeMissingFieldDocuments judgeMissingFieldDocuments = new JudgeMissingFieldDocuments();

        for (Map<String, Object> map : result) {
            if (judgeMissingFieldDocuments.hasMissingValue(map)) {
                missingCount++;
            }
        }

        this.missingFieldDocuments = missingCount;
    }

    public void calDuplicateDocuments(){
        class JudgeDuplicationDocuments{
            public boolean mapsAreEqual(Map<String, Object> map1, Map<String, Object> map2) {
                // 如果两个 Map 的键值对完全相同，则认为它们相等
                return map1.equals(map2);
            }

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
        JudgeDuplicationDocuments judgeDuplicationDocuments = new JudgeDuplicationDocuments();
        for (Map<String, Object> element : result) {
            if (judgeDuplicationDocuments.containsElement(checkedElements, element)) {
                duplicateCount++;
            } else {
                checkedElements.add(element);
            }
        }

        this.duplicateDocuments = duplicateCount;
    }
    @Override
    public double dataQualityScore() {
        if (result.isEmpty())   _getData();

        if(this.totalDocuments == ILLEGAL_CODE) calTotalDocuments();
        if(this.missingFieldDocuments == ILLEGAL_CODE)  calMissingFieldDocuments();
        if(this.duplicateDocuments == ILLEGAL_CODE)  calDuplicateDocuments();
        double score = 100.0 - ((double) (missingFieldDocuments + duplicateDocuments) / totalDocuments * 100);
        setDataQualityScore(score);
        return score > 0 ? score : 0;
    }
}
