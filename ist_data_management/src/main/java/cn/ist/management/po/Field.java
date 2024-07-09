package cn.ist.management.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field {

    public static final Integer TYPE_NUMBER = 1;
    public static final Integer TYPE_STRING = 2;

    private String name;
    private Integer type;
    private String description;
    // 导出到文件中时进行脱敏
    private Boolean sensitive;
    // 存到系统中时进行加密
    private Boolean encrypt;
    // 用户查看时进行密级的判定，用户的level比字段的secretLevel高才能看到
    private Integer secretLevel;

    public String accordingPostgresqlType() {
        if (type.equals(TYPE_NUMBER)) {
            return "REAL";
        }
        if (type.equals(TYPE_STRING)) {
            return "VARCHAR(255)";
        }
        return null;
    }

    public Field(String name, Integer type) {
        this.name = name;
        this.type = type;
    }

}
