package cn.ist.management.po.quality;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.SQLException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DataQuality {
    /*
    * 数据质量包含对数据源质量的监控，不涉及对数据源的清洗和修改
    * 数据规则包含对数据源的修改，不返回对数据质量的监控
    * */
    public static final Integer ILLEGAL_CODE = -1;
    public static final String ILLEGAL_URL = "-1";
    public static final Integer MODAL_TEMPORAL = 1;
    public static final Integer MODAL_IMAGE = 2;
    public static final Integer MODAL_TEXT = 3;
    public static final Integer TYPE_STRUCTURED = 1;
    public static final Integer TYPE_SEMI_STRUCTURED = 2;
    public static final Integer TYPE_UNSTRUCTURED = 3;
    public static final Integer TYPE_TEMPORAL = 4;

    private Integer dataType = ILLEGAL_CODE;
    private Integer modalType = ILLEGAL_CODE;
    private String dataSourceUrl = ILLEGAL_URL; // 标识数据源

    private double dataQualityScore = ILLEGAL_CODE;

    public DataQuality(String url) {
        this.dataSourceUrl = url;
    }

    public abstract double dataQualityScore() throws SQLException;

}
