package cn.ist.management.vo.fromFront;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCronSaveJobFromVo {
    private String modelId;
    private String cron;
}
