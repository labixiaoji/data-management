package cn.ist.management.po.quality;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class UnstructuredDataQuality extends DataQuality {

    /*  待定义 */

    private int totalFileCount = ILLEGAL_CODE;
    private int corruptFileCount = ILLEGAL_CODE;
    private int unreadableFileCount = ILLEGAL_CODE;
    private int inconsistentMetadataCount = ILLEGAL_CODE;
    private double averageFileSize = ILLEGAL_CODE;
    private double dataQualityScore = ILLEGAL_CODE;

    public UnstructuredDataQuality(String url, Integer modalType) {
        super(url);
        setModalType(modalType);
        setDataType(TYPE_UNSTRUCTURED);
    }


    @Override
    public double dataQualityScore() {
        double score = 100.0 - ((double) (corruptFileCount + unreadableFileCount + inconsistentMetadataCount) / totalFileCount * 100);
        setDataQualityScore(score);
        return score > 0 ? score : 0;
    }
}
